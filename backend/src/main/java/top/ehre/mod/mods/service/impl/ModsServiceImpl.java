package top.ehre.mod.mods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.ehre.mod.category.domain.entity.CategoryEntity;
import top.ehre.mod.category.service.CategoryService;
import top.ehre.mod.mods.domain.entity.ModVersionEntity;
import top.ehre.mod.mods.domain.entity.ModsEntity;
import top.ehre.mod.mods.mapper.ModVersionMapper;
import top.ehre.mod.mods.mapper.ModsMapper;
import top.ehre.mod.mods.service.ModsService;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import top.ehre.mod.mods.domain.vo.ModVersionVO;
import top.ehre.mod.mods.domain.vo.ModsVO;
import top.ehre.mod.mods.domain.vo.ModsCountVO;
import top.ehre.mod.mods.domain.dto.ModVersionAddDTO;
import top.ehre.mod.mods.domain.dto.ModVersionSaveDTO;
import top.ehre.mod.mods.domain.dto.ModVersionUpdateDTO;
import top.ehre.mod.mods.domain.dto.ModsPageDTO;
import top.ehre.mod.mods.domain.dto.ModsAddDTO;
import top.ehre.mod.mods.domain.dto.ModsUpdateDTO;
import top.ehre.mod.security.authentication.UserInfo;
import top.ehre.mod.system.user.service.UserService;
import top.ehre.mod.tag.domain.entity.TagEntity;
import top.ehre.mod.tag.domain.vo.TagVO;
import top.ehre.mod.tag.service.TagService;
import top.ehre.mod.util.IPUtil;
import top.ehre.mod.util.PageResult;
import top.ehre.mod.util.PageUtil;
import top.ehre.mod.exception.BusinessException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 *  服务实现类
 *
 * @author LibrhHp_0928
 * @since 2025-11-24 18:58:33
 */
@Service
public class ModsServiceImpl extends ServiceImpl<ModsMapper, ModsEntity> implements ModsService {

    @Resource
    ModsMapper modsMapper;

    @Resource
    ModVersionMapper modVersionMapper;

    @Resource
    UserService userService;

    @Resource
    CategoryService categoryService;

    @Resource
    TagService tagService;

    @Resource
    RedisTemplate redisTemplate;

    private static final long VIEW_INTERVAL_MINUTES = 30;
    private static final long DOWNLOAD_INTERVAL_MINUTES = 10;
    /** 模组超级管理权限，拥有后可查看/管理全部模组 */
    private static final String MOD_SUPER_CODE = "business:mod:sup";
    private static final String ADMIN_ROLE_ID = "1";
    private static final String ADMIN_ROLE_NAME = "管理员";
    private static final int VERSION_DESCRIPTION_MAX = 2000;
    private static final int VERSION_URL_MAX = 500;
    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+(-[a-zA-Z]+)?$");
    private static final Pattern URL_PATTERN = Pattern.compile("^https?://\\S+$");


    @Override
    public PageResult<ModsVO> page(ModsPageDTO modsPageDTO) {
        applyOwnerScope(modsPageDTO);
        Page<?> page = PageUtil.convert2PageQuery(modsPageDTO);
        List<ModsVO> list = modsMapper.queryPage(page, modsPageDTO);
        for(ModsVO modsVO : list){
            modsVO.setOtherAuthors(modsMapper.getOtherAuthors(modsVO.getId()));
            fillTags(modsVO);
        }
        fillVersions(list);
        PageResult<ModsVO> pageResult = PageUtil.convert2PageResult(page, list);
        return pageResult;
    }

    @Override
    public List<ModsVO> getList() {
        return getListByCategory(null);
    }

    @Override
    public List<ModsVO> getListByCategory(String categoryId) {
        QueryWrapper<ModsEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_visible", true);
        if (categoryId != null && !categoryId.isBlank()) {
            validateCategoryId(categoryId);
            queryWrapper.eq("category_id", categoryId);
        }
        queryWrapper.orderByDesc("is_featured")
                .orderByDesc("updated_at");
        List<ModsEntity> list = modsMapper.selectList(queryWrapper);
        List<ModsVO> result = list.stream().map(this::toPublicModsVO).toList();
        fillVersions(result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean add(ModsAddDTO modsAddDTO) {
        ModsEntity mods = new ModsEntity();
        UserInfo userInfo = currentUserInfo();
        if (userInfo != null) {
            String currentUserId = userInfo.getUser().getUserId();
            if (!canManageAllMods(userInfo)
                    || modsAddDTO.getAuthorId() == null
                    || modsAddDTO.getAuthorId().isBlank()) {
                modsAddDTO.setAuthorId(currentUserId);
            }
        }
        BeanUtils.copyProperties(modsAddDTO, mods);
        normalizeCategoryId(mods);
        validateCategoryId(mods.getCategoryId());
        boolean saved = save(mods);
        if (!saved) {
            throw new BusinessException("添加失败");
        }
        saveModTags(mods.getId(), modsAddDTO.getTagIds());
        if (modsAddDTO.getVersions() != null) {
            syncVersions(mods, modsAddDTO.getVersions());
        } else {
            backfillCurrentVersion(mods);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean delete(String id) {
        ModsEntity exists = getById(id);
        if (exists == null) throw new BusinessException("不存在该对象");
        assertCanDelete(exists);
        modsMapper.deleteModTags(id);
        deleteVersionsByModId(id);
        boolean removed = removeById(id);
        if (!removed) throw new BusinessException("删除失败");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean batchDelete(List<String> ids) {
        if (ids != null) {
            for (String id : ids) {
                ModsEntity exists = getById(id);
                if (exists == null) throw new BusinessException("不存在该对象");
                assertCanDelete(exists);
                modsMapper.deleteModTags(id);
                deleteVersionsByModId(id);
            }
        }
        boolean removed = removeBatchByIds(ids);
        if (!removed) throw new BusinessException("删除失败");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean update(ModsUpdateDTO modsUpdateDTO) {
        ModsEntity exists = getById(modsUpdateDTO.getId());
        if (exists == null) throw new BusinessException("不存在该对象");
        UserInfo userInfo = currentUserInfo();
        assertCanUpdate(exists);
        if (!canManageAllMods(userInfo)) {
            modsUpdateDTO.setAuthorId(exists.getAuthorId());
            modsUpdateDTO.setIsFeatured(exists.getIsFeatured());
            modsUpdateDTO.setIsPreposition(exists.getIsPreposition());
        }
        if (modsUpdateDTO.getOtherAuthors() != null) {
            modsMapper.deleteOtherAuthor(modsUpdateDTO.getId());
            modsUpdateDTO.getOtherAuthors().forEach(authorId -> {
                addOtherAuthor(modsUpdateDTO.getId(), authorId);
            });
        }
        ModsEntity mods = new ModsEntity();
        BeanUtils.copyProperties(modsUpdateDTO, mods);
        normalizeCategoryId(mods);
        validateCategoryId(mods.getCategoryId());
        if (mods.getId() == null) throw new BusinessException("主键不能为空");
        if (modsUpdateDTO.getTagIds() != null) {
            saveModTags(mods.getId(), modsUpdateDTO.getTagIds());
        }
        boolean updated = updateById(mods);
        if (!updated) {
            throw new BusinessException("更新失败");
        }
        if (modsUpdateDTO.getVersions() != null) {
            syncVersions(exists, modsUpdateDTO.getVersions());
        }
        return true;
    }

    @Override
    public ModsVO get(String id) {
        ModsEntity mods = getById(id);
        if (mods == null) {
            throw new BusinessException("不存在该对象");
        }
        if (!Boolean.TRUE.equals(mods.getIsVisible())) {
            assertCanUpdate(mods);
        }
        ModsVO modsVO = new ModsVO();
        BeanUtils.copyProperties(mods, modsVO);
        UserInfo userInfo = currentUserInfo();
        if (userInfo != null) {
            modsVO.setAuthorName(userService.get(mods.getAuthorId()).getNickname());
        }
        fillCategoryName(modsVO);
        fillTags(modsVO);
        fillVersions(List.of(modsVO));
        return modsVO;
    }

    @Override
    public List<ModVersionVO> listVersions(String modId) {
        ModsEntity mods = getById(modId);
        if (mods == null) {
            throw new BusinessException("不存在该对象");
        }
        assertCanUpdate(mods);
        return toVersionVOList(listVersionEntities(modId), mods.getVersion(), mods);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ModVersionVO addVersion(String modId, ModVersionAddDTO modVersionAddDTO) {
        ModsEntity mods = getById(modId);
        if (mods == null) {
            throw new BusinessException("不存在该对象");
        }
        assertCanUpdate(mods);
        String version = normalizeVersion(modVersionAddDTO.getVersion());
        String directUrl = normalizeUrl(modVersionAddDTO.getDownloadDirectUrl(), "直链下载地址");
        String cloudUrl = normalizeUrl(modVersionAddDTO.getDownloadCloudUrl(), "网盘下载地址");
        String description = normalizeDescription(modVersionAddDTO.getDescription());
        List<ModVersionEntity> existing = listVersionEntities(modId);
        if (existing.stream().anyMatch(item -> version.equals(item.getVersion()))) {
            throw new BusinessException("版本号重复");
        }
        if (existing.isEmpty()) {
            backfillCurrentVersion(mods, version);
        }
        ModVersionEntity entity = insertVersion(modId, version, description, directUrl, cloudUrl);
        boolean makeCurrent = modVersionAddDTO.getCurrent() == null || Boolean.TRUE.equals(modVersionAddDTO.getCurrent());
        if (makeCurrent) {
            modsMapper.refreshModForNewVersion(modId, version, directUrl, cloudUrl);
        } else {
            modsMapper.touchUpdatedAt(modId);
        }
        return toVersionVO(entity, makeCurrent ? version : mods.getVersion());
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean updateVersion(ModVersionUpdateDTO modVersionUpdateDTO) {
        if (modVersionUpdateDTO.getId() == null || modVersionUpdateDTO.getId().isBlank()) {
            throw new BusinessException("主键不能为空");
        }
        ModVersionEntity exists = modVersionMapper.selectById(modVersionUpdateDTO.getId());
        if (exists == null) {
            throw new BusinessException("不存在该对象");
        }
        ModsEntity mods = getById(exists.getModId());
        if (mods == null) {
            throw new BusinessException("不存在该对象");
        }
        assertCanUpdate(mods);
        String version = normalizeVersion(modVersionUpdateDTO.getVersion());
        String directUrl = normalizeUrl(modVersionUpdateDTO.getDownloadDirectUrl(), "直链下载地址");
        String cloudUrl = normalizeUrl(modVersionUpdateDTO.getDownloadCloudUrl(), "网盘下载地址");
        assertVersionAvailable(exists.getModId(), version, exists.getId());
        String previousVersion = exists.getVersion();
        exists.setVersion(version);
        exists.setDescription(normalizeDescription(modVersionUpdateDTO.getDescription()));
        exists.setDownloadDirectUrl(directUrl);
        exists.setDownloadCloudUrl(cloudUrl);
        if (modVersionMapper.updateById(exists) == 0) {
            throw new BusinessException("更新失败");
        }
        boolean current = Boolean.TRUE.equals(modVersionUpdateDTO.getCurrent())
                || Objects.equals(mods.getVersion(), previousVersion);
        if (current) {
            modsMapper.syncCurrentVersion(mods.getId(), version, directUrl, cloudUrl);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean deleteVersion(String id) {
        ModVersionEntity exists = modVersionMapper.selectById(id);
        if (exists == null) {
            throw new BusinessException("不存在该对象");
        }
        ModsEntity mods = getById(exists.getModId());
        if (mods == null) {
            throw new BusinessException("不存在该对象");
        }
        assertCanUpdate(mods);
        long count = modVersionMapper.selectCount(new LambdaQueryWrapper<ModVersionEntity>()
                .eq(ModVersionEntity::getModId, exists.getModId()));
        if (count <= 1) {
            throw new BusinessException("至少保留一个版本");
        }
        if (modVersionMapper.deleteById(id) == 0) {
            throw new BusinessException("删除失败");
        }
        if (Objects.equals(mods.getVersion(), exists.getVersion())) {
            List<ModVersionEntity> remaining = listVersionEntities(exists.getModId());
            if (!remaining.isEmpty()) {
                ModVersionEntity latest = remaining.get(0);
                modsMapper.syncCurrentVersion(mods.getId(), latest.getVersion(),
                        latest.getDownloadDirectUrl(), latest.getDownloadCloudUrl());
            }
        }
        return true;
    }

    @Override
    public int addOtherAuthor(String id, String authorId) {
        return modsMapper.addOtherAuthor(id, authorId);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ModsCountVO incrementDownloadCount(String id) {
        ModsEntity mods = requirePublishedMod(id);
        if (!tryAcquireCount("download", id, DOWNLOAD_INTERVAL_MINUTES)) {
            return toCountVO(mods);
        }
        int rows = modsMapper.incrementDownloadCount(id);
        if (rows == 0) {
            throw new BusinessException("更新失败");
        }
        return toCountVO(getById(id));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ModsCountVO incrementViewCount(String id) {
        ModsEntity mods = requirePublishedMod(id);
        if (!tryAcquireCount("view", id, VIEW_INTERVAL_MINUTES)) {
            return toCountVO(mods);
        }
        int rows = modsMapper.incrementViewCount(id);
        if (rows == 0) {
            throw new BusinessException("更新失败");
        }
        return toCountVO(getById(id));
    }

    private ModsEntity requirePublishedMod(String id) {
        ModsEntity mods = getById(id);
        if (mods == null) {
            throw new BusinessException("不存在该对象");
        }
        if (!Boolean.TRUE.equals(mods.getIsVisible())) {
            throw new BusinessException("模组未发布");
        }
        return mods;
    }

    /**
     * 同一 IP 对同一模组在冷却时间内只计一次。
     * 重复请求返回 false，不增加计数。
     */
    private boolean tryAcquireCount(String type, String modId, long minutes) {
        String key = "mod:count:" + type + ":" + modId + ":" + currentIp();
        Boolean first = redisTemplate.opsForValue().setIfAbsent(key, "1", minutes, TimeUnit.MINUTES);
        return Boolean.TRUE.equals(first);
    }

    private String currentIp() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            String ip = IPUtil.getIP(request);
            if (ip != null && !ip.isBlank() && !"未知".equals(ip)) {
                return ip;
            }
        }
        return "unknown";
    }

    private ModsCountVO toCountVO(ModsEntity mods) {
        return new ModsCountVO()
                .setId(mods.getId())
                .setDownloadCount(mods.getDownloadCount())
                .setViewCount(mods.getViewCount());
    }

    private ModsVO toPublicModsVO(ModsEntity mods) {
        ModsVO modsVO = new ModsVO();
        BeanUtils.copyProperties(mods, modsVO);
        List<String> authorIds = modsMapper.getOtherAuthors(mods.getId());
        String otherAuthorsName = "";
        if (authorIds != null && authorIds.size() > 0) {
            otherAuthorsName = "、";
            for (String authorId : authorIds) {
                if (!Objects.equals(authorId, mods.getAuthorId())) {
                    otherAuthorsName += userService.get(authorId).getNickname() + "、";
                }
            }
            otherAuthorsName = otherAuthorsName.substring(0, otherAuthorsName.length() - 1);
        }
        modsVO.setAuthorName(userService.get(mods.getAuthorId()).getNickname() + otherAuthorsName);
        fillCategoryName(modsVO);
        fillTags(modsVO);
        return modsVO;
    }

    private void normalizeCategoryId(ModsEntity mods) {
        if (mods.getCategoryId() != null && mods.getCategoryId().isBlank()) {
            mods.setCategoryId(null);
        }
    }

    private void validateCategoryId(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return;
        }
        if (categoryService.getById(categoryId) == null) {
            throw new BusinessException("分类不存在");
        }
    }

    private void saveModTags(String modId, List<String> tagIds) {
        modsMapper.deleteModTags(modId);
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        List<String> distinctIds = tagIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (distinctIds.isEmpty()) {
            return;
        }
        long exists = tagService.count(new LambdaQueryWrapper<TagEntity>().in(TagEntity::getId, distinctIds));
        if (exists != distinctIds.size()) {
            throw new BusinessException("存在无效的标签，只能从已有标签中选择");
        }
        distinctIds.forEach(tagId -> modsMapper.addModTag(modId, tagId));
    }

    private void syncVersions(ModsEntity before, List<ModVersionSaveDTO> versions) {
        if (versions.isEmpty()) {
            throw new BusinessException("至少保留一个版本");
        }
        List<ModVersionSaveDTO> normalized = new ArrayList<>();
        Set<String> requestVersions = new HashSet<>();
        for (ModVersionSaveDTO item : versions) {
            ModVersionSaveDTO copy = new ModVersionSaveDTO();
            copy.setId(blankToNull(item.getId()));
            copy.setVersion(normalizeVersion(item.getVersion()));
            copy.setDescription(normalizeDescription(item.getDescription()));
            copy.setDownloadDirectUrl(normalizeUrl(item.getDownloadDirectUrl(), "直链下载地址"));
            copy.setDownloadCloudUrl(normalizeUrl(item.getDownloadCloudUrl(), "网盘下载地址"));
            copy.setCurrent(item.getCurrent());
            if (!requestVersions.add(copy.getVersion())) {
                throw new BusinessException("版本号重复");
            }
            normalized.add(copy);
        }
        String modId = before.getId();
        List<ModVersionEntity> existing = listVersionEntities(modId);
        Map<String, ModVersionEntity> byId = new HashMap<>();
        Set<String> knownVersions = new HashSet<>();
        for (ModVersionEntity item : existing) {
            byId.put(item.getId(), item);
            knownVersions.add(item.getVersion());
        }
        if (knownVersions.isEmpty() && before.getVersion() != null && !before.getVersion().isBlank()) {
            knownVersions.add(before.getVersion().trim());
        }
        boolean addedNew = normalized.stream().anyMatch(item -> !knownVersions.contains(item.getVersion()));
        Map<String, ModVersionEntity> byVersion = new HashMap<>();
        for (ModVersionEntity item : existing) {
            byVersion.put(item.getVersion(), item);
        }
        Set<String> matchedIds = new HashSet<>();
        Set<String> versionChangedIds = new HashSet<>();
        List<ModVersionSaveDTO> inserts = new ArrayList<>();
        List<ModVersionEntity> updates = new ArrayList<>();
        for (ModVersionSaveDTO item : normalized) {
            ModVersionEntity matched = item.getId() == null ? null : byId.get(item.getId());
            if (matched == null) {
                matched = byVersion.get(item.getVersion());
            }
            if (matched != null) {
                if (!matchedIds.add(matched.getId())) {
                    throw new BusinessException("版本号重复");
                }
                String originalVersion = matched.getVersion();
                byVersion.remove(originalVersion);
                if (!originalVersion.equals(item.getVersion())) {
                    versionChangedIds.add(matched.getId());
                }
                matched.setVersion(item.getVersion());
                matched.setDescription(item.getDescription());
                matched.setDownloadDirectUrl(item.getDownloadDirectUrl());
                matched.setDownloadCloudUrl(item.getDownloadCloudUrl());
                updates.add(matched);
            } else {
                inserts.add(item);
            }
        }
        for (ModVersionEntity item : existing) {
            if (!matchedIds.contains(item.getId())) {
                modVersionMapper.deleteById(item.getId());
            }
        }
        for (String changedId : versionChangedIds) {
            modVersionMapper.update(null, new LambdaUpdateWrapper<ModVersionEntity>()
                    .eq(ModVersionEntity::getId, changedId)
                    .set(ModVersionEntity::getVersion, "__tmp_" + changedId));
        }
        for (ModVersionEntity item : updates) {
            assertVersionAvailable(modId, item.getVersion(), item.getId());
            if (modVersionMapper.updateById(item) == 0) {
                throw new BusinessException("更新失败");
            }
        }
        for (ModVersionSaveDTO item : inserts) {
            insertVersion(modId, item.getVersion(), item.getDescription(),
                    item.getDownloadDirectUrl(), item.getDownloadCloudUrl());
        }
        ModVersionSaveDTO current = normalized.stream()
                .filter(item -> Boolean.TRUE.equals(item.getCurrent()))
                .findFirst()
                .orElse(normalized.get(0));
        if (addedNew) {
            modsMapper.refreshModForNewVersion(modId, current.getVersion(),
                    current.getDownloadDirectUrl(), current.getDownloadCloudUrl());
        } else {
            modsMapper.syncCurrentVersion(modId, current.getVersion(),
                    current.getDownloadDirectUrl(), current.getDownloadCloudUrl());
        }
    }

    private void backfillCurrentVersion(ModsEntity mods) {
        backfillCurrentVersion(mods, null);
    }

    /**
     * 旧模组还没有版本行时，把当前发布信息补成一条记录。
     * skipVersion 已由本次新增写入时不再重复补。
     */
    private void backfillCurrentVersion(ModsEntity mods, String skipVersion) {
        if (mods.getVersion() == null || mods.getVersion().isBlank()) {
            return;
        }
        String version = mods.getVersion().trim();
        if (version.equals(skipVersion)) {
            return;
        }
        Long count = modVersionMapper.selectCount(new LambdaQueryWrapper<ModVersionEntity>()
                .eq(ModVersionEntity::getModId, mods.getId())
                .eq(ModVersionEntity::getVersion, version));
        if (count != null && count > 0) {
            return;
        }
        insertVersion(mods.getId(), version, null, mods.getDownloadDirectUrl(), mods.getDownloadCloudUrl());
    }

    private ModVersionEntity insertVersion(String modId, String version, String description,
                                           String directUrl, String cloudUrl) {
        ModVersionEntity entity = new ModVersionEntity();
        entity.setModId(modId);
        entity.setVersion(version);
        entity.setDescription(description);
        entity.setDownloadDirectUrl(directUrl);
        entity.setDownloadCloudUrl(cloudUrl);
        if (modVersionMapper.insert(entity) == 0) {
            throw new BusinessException("添加失败");
        }
        return entity;
    }

    private void assertVersionAvailable(String modId, String version, String excludeId) {
        LambdaQueryWrapper<ModVersionEntity> wrapper = new LambdaQueryWrapper<ModVersionEntity>()
                .eq(ModVersionEntity::getModId, modId)
                .eq(ModVersionEntity::getVersion, version);
        if (excludeId != null) {
            wrapper.ne(ModVersionEntity::getId, excludeId);
        }
        Long count = modVersionMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException("版本号重复");
        }
    }

    private List<ModVersionEntity> listVersionEntities(String modId) {
        return modVersionMapper.selectList(new LambdaQueryWrapper<ModVersionEntity>()
                .eq(ModVersionEntity::getModId, modId)
                .orderByDesc(ModVersionEntity::getId));
    }

    private void deleteVersionsByModId(String modId) {
        modVersionMapper.delete(new LambdaQueryWrapper<ModVersionEntity>()
                .eq(ModVersionEntity::getModId, modId));
    }

    private void fillVersions(List<ModsVO> modsList) {
        if (modsList == null || modsList.isEmpty()) {
            return;
        }
        List<String> modIds = modsList.stream()
                .map(ModsVO::getId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        Map<String, List<ModVersionEntity>> grouped = new HashMap<>();
        if (!modIds.isEmpty()) {
            List<ModVersionEntity> versions = modVersionMapper.selectList(new LambdaQueryWrapper<ModVersionEntity>()
                    .in(ModVersionEntity::getModId, modIds)
                    .orderByDesc(ModVersionEntity::getId));
            for (ModVersionEntity version : versions) {
                grouped.computeIfAbsent(version.getModId(), key -> new ArrayList<>()).add(version);
            }
        }
        for (ModsVO mods : modsList) {
            List<ModVersionEntity> entities = grouped.get(mods.getId());
            mods.setVersions(toVersionVOList(entities, mods.getVersion(), null));
            if (mods.getVersions().isEmpty()) {
                mods.setVersions(fallbackVersions(mods));
            }
        }
    }

    private List<ModVersionVO> toVersionVOList(List<ModVersionEntity> entities, String currentVersion, ModsEntity fallback) {
        if (entities == null || entities.isEmpty()) {
            if (fallback == null) {
                return new ArrayList<>();
            }
            ModsVO modsVO = new ModsVO();
            BeanUtils.copyProperties(fallback, modsVO);
            return fallbackVersions(modsVO);
        }
        List<ModVersionVO> result = new ArrayList<>();
        for (ModVersionEntity entity : entities) {
            result.add(toVersionVO(entity, currentVersion));
        }
        return result;
    }

    private ModVersionVO toVersionVO(ModVersionEntity entity, String currentVersion) {
        ModVersionVO vo = new ModVersionVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setCurrent(Objects.equals(entity.getVersion(), currentVersion));
        return vo;
    }

    private List<ModVersionVO> fallbackVersions(ModsVO mods) {
        boolean hasVersion = mods.getVersion() != null && !mods.getVersion().isBlank();
        boolean hasDirect = mods.getDownloadDirectUrl() != null && !mods.getDownloadDirectUrl().isBlank();
        boolean hasCloud = mods.getDownloadCloudUrl() != null && !mods.getDownloadCloudUrl().isBlank();
        if (!hasVersion && !hasDirect && !hasCloud) {
            return new ArrayList<>();
        }
        ModVersionVO vo = new ModVersionVO();
        vo.setModId(mods.getId());
        vo.setVersion(mods.getVersion());
        vo.setDownloadDirectUrl(mods.getDownloadDirectUrl());
        vo.setDownloadCloudUrl(mods.getDownloadCloudUrl());
        vo.setCurrent(true);
        return List.of(vo);
    }

    private String normalizeVersion(String version) {
        if (version == null || version.isBlank()) {
            throw new BusinessException("版本号不能为空");
        }
        String value = version.trim();
        if (!VERSION_PATTERN.matcher(value).matches()) {
            throw new BusinessException("版本格式不正确，应为 X.Y.Z 或 X.Y.Z-类型");
        }
        return value;
    }

    private String normalizeUrl(String url, String label) {
        if (url == null || url.isBlank()) {
            throw new BusinessException(label + "不能为空");
        }
        String value = url.trim();
        if (value.length() > VERSION_URL_MAX || !URL_PATTERN.matcher(value).matches()) {
            throw new BusinessException(label + "格式不正确");
        }
        return value;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        String value = description.trim();
        if (value.length() > VERSION_DESCRIPTION_MAX) {
            throw new BusinessException("版本描述不能超过2000字");
        }
        return value;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void fillTags(ModsVO modsVO) {
        List<TagVO> tags = modsMapper.getTags(modsVO.getId());
        if (tags == null) {
            tags = new ArrayList<>();
        }
        modsVO.setTags(tags);
        modsVO.setTagIds(tags.stream().map(TagVO::getId).toList());
    }

    private void fillCategoryName(ModsVO modsVO) {
        if (modsVO.getCategoryName() != null || modsVO.getCategoryId() == null || modsVO.getCategoryId().isBlank()) {
            return;
        }
        CategoryEntity category = categoryService.getById(modsVO.getCategoryId());
        if (category != null) {
            modsVO.setCategoryName(category.getName());
        }
    }

    /**
     * 管理端列表：无超级管理权限时，只返回当前用户作为作者或共创的模组。
     * 公共接口不按作者收窄。
     */
    private void applyOwnerScope(ModsPageDTO modsPageDTO) {
        modsPageDTO.setScopeUserId(null);
        if (isPublicRequest()) {
            return;
        }
        UserInfo userInfo = currentUserInfo();
        if (userInfo != null && !canManageAllMods(userInfo)) {
            modsPageDTO.setScopeUserId(userInfo.getUser().getUserId());
        }
    }

    private boolean isPublicRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            String uri = servletAttributes.getRequest().getRequestURI();
            return uri != null && uri.contains("/public/");
        }
        return false;
    }

    private void assertCanUpdate(ModsEntity mods) {
        UserInfo userInfo = currentUserInfo();
        if (canManageAllMods(userInfo)) {
            return;
        }
        if (userInfo == null || !isAuthorOrCollaborator(mods, userInfo.getUser().getUserId())) {
            throw new BusinessException("无权限");
        }
    }

    private void assertCanDelete(ModsEntity mods) {
        UserInfo userInfo = currentUserInfo();
        if (canManageAllMods(userInfo)) {
            return;
        }
        if (userInfo == null || !Objects.equals(mods.getAuthorId(), userInfo.getUser().getUserId())) {
            throw new BusinessException("无权限");
        }
    }

    private boolean isAuthorOrCollaborator(ModsEntity mods, String userId) {
        if (mods == null || userId == null) {
            return false;
        }
        if (userId.equals(mods.getAuthorId())) {
            return true;
        }
        List<String> otherAuthors = modsMapper.getOtherAuthors(mods.getId());
        return otherAuthors != null && otherAuthors.contains(userId);
    }

    /**
     * 拥有 business:mod:sup 权限，或具备管理员角色时，可管理全部模组。
     */
    private boolean canManageAllMods(UserInfo userInfo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> MOD_SUPER_CODE.equals(authority.getAuthority()))) {
            return true;
        }
        if (userInfo == null) {
            return false;
        }
        if (userInfo.getMenus() != null && userInfo.getMenus().stream()
                .anyMatch(menu -> MOD_SUPER_CODE.equals(menu.getCode()))) {
            return true;
        }
        if (userInfo.getRoles() == null) {
            return false;
        }
        return userInfo.getRoles().stream().anyMatch(role ->
                ADMIN_ROLE_ID.equals(role.getRoleId()) || ADMIN_ROLE_NAME.equals(role.getRoleName()));
    }

    private UserInfo currentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        if (authentication.getDetails() instanceof UserInfo userInfo) {
            return userInfo;
        }
        try {
            return userService.getUserInfo(authentication.getName());
        } catch (BusinessException e) {
            return null;
        }
    }
}
