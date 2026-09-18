package top.ehre.mod.mods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.ehre.mod.category.domain.entity.CategoryEntity;
import top.ehre.mod.category.service.CategoryService;
import top.ehre.mod.mods.domain.entity.ModsEntity;
import top.ehre.mod.mods.mapper.ModsMapper;
import top.ehre.mod.mods.service.ModsService;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import top.ehre.mod.mods.domain.vo.ModsVO;
import top.ehre.mod.mods.domain.vo.ModsCountVO;
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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
    UserService userService;

    @Resource
    CategoryService categoryService;

    @Resource
    TagService tagService;

    @Resource
    RedisTemplate redisTemplate;

    private static final long VIEW_INTERVAL_MINUTES = 30;
    private static final long DOWNLOAD_INTERVAL_MINUTES = 10;


    @Override
    public PageResult<ModsVO> page(ModsPageDTO modsPageDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            UserInfo userInfo = userService.getUserInfo(authentication.getName());
            if (!"1".equals(userInfo.getUser().getUserId())){
                modsPageDTO.setAuthorId(userInfo.getUser().getUserId());
            }
        }
        Page<?> page = PageUtil.convert2PageQuery(modsPageDTO);
        List<ModsVO> list = modsMapper.queryPage(page, modsPageDTO);
        for(ModsVO modsVO : list){
            modsVO.setOtherAuthors(modsMapper.getOtherAuthors(modsVO.getId()));
            fillTags(modsVO);
        }
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
        return list.stream().map(this::toPublicModsVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean add(ModsAddDTO modsAddDTO) {
        ModsEntity mods = new ModsEntity();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            UserInfo userInfo = userService.getUserInfo(authentication.getName());
            modsAddDTO.setAuthorId(userInfo.getUser().getUserId());
        }
        BeanUtils.copyProperties(modsAddDTO, mods);
        normalizeCategoryId(mods);
        validateCategoryId(mods.getCategoryId());
        boolean saved = save(mods);
        if (!saved) {
            throw new BusinessException("添加失败");
        }
        saveModTags(mods.getId(), modsAddDTO.getTagIds());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean delete(String id) {
        ModsEntity exists = getById(id);
        if (exists == null) throw new BusinessException("不存在该对象");
        modsMapper.deleteModTags(id);
        boolean removed = removeById(id);
        if (!removed) throw new BusinessException("删除失败");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean batchDelete(List<String> ids) {
        if (ids != null) {
            ids.forEach(modsMapper::deleteModTags);
        }
        boolean removed = removeBatchByIds(ids);
        if (!removed) throw new BusinessException("删除失败");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean update(ModsUpdateDTO modsUpdateDTO) {
        // 鉴权 只有是自己的mod才可以编辑，超级管理员则不受影响
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            UserInfo userInfo = userService.getUserInfo(authentication.getName());
            if (!"1".equals(userInfo.getUser().getUserId())){
                ModsEntity mods = getById(modsUpdateDTO.getId());
                if (!mods.getAuthorId().equals(userInfo.getUser().getUserId())){
                    throw new BusinessException("无权限");
                }
            }
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
        else {
            ModsEntity exists = getById(mods.getId());
            if (exists == null) throw new BusinessException("不存在该对象");
        }
        if (modsUpdateDTO.getTagIds() != null) {
            saveModTags(mods.getId(), modsUpdateDTO.getTagIds());
        }
        boolean updated = updateById(mods);
        if (!updated) {
            throw new BusinessException("更新失败");
        }
        return true;
    }

    @Override
    public ModsVO get(String id) {
        ModsEntity mods = getById(id);
        if (mods == null) {
            throw new BusinessException("不存在该对象");
        }
        ModsVO modsVO = new ModsVO();
        BeanUtils.copyProperties(mods, modsVO);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            modsVO.setAuthorName(userService.get(mods.getAuthorId()).getNickname());
        }
        fillCategoryName(modsVO);
        fillTags(modsVO);
        return modsVO;
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
}
