package top.ehre.mod.system.user.service.impl;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import top.ehre.mod.exception.BusinessException;
import top.ehre.mod.security.authentication.UserInfo;
import top.ehre.mod.system.menu.domain.entity.MenuEntity;
import top.ehre.mod.system.menu.mapper.MenuMapper;
import top.ehre.mod.system.role.domain.entity.RoleEntity;
import top.ehre.mod.system.role.domain.vo.RoleVO;
import top.ehre.mod.system.role.mapper.RoleMapper;
import top.ehre.mod.system.setting.service.SystemSettingService;
import top.ehre.mod.system.user.domain.dto.RegisterDTO;
import top.ehre.mod.system.user.domain.dto.UserAddDTO;
import top.ehre.mod.system.user.domain.dto.UserPageDTO;
import top.ehre.mod.system.user.domain.dto.UserUpdateDTO;
import top.ehre.mod.system.user.domain.entity.UserEntity;
import top.ehre.mod.system.user.domain.vo.UserVO;
import top.ehre.mod.system.user.mapper.UserMapper;
import top.ehre.mod.system.user.service.UserService;
import top.ehre.mod.system.userRole.domain.entity.UserRoleEntity;
import top.ehre.mod.system.userRole.mapper.UserRoleMapper;
import top.ehre.mod.util.PageResult;
import top.ehre.mod.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 用户表 服务实现类
 *
 * @author LibrhHp_0928
 * @since 2025-11-24 18:58:32
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Resource
    UserMapper userMapper;

    // ---------------------- 联表注入 -------------------------
    @Resource
    RoleMapper roleMapper;
    @Resource
    UserRoleMapper userRoleMapper;
    // ---------------------------------------------------------


    @Resource
    MenuMapper menuMapper;

    @Resource
    SystemSettingService systemSettingService;

    @Resource
    @Lazy
    private PasswordEncoder passwordEncoder;

    // 注入url
    @Value("${push.url}")
    private String pushUrl;

    private void setRoleVOByUserRoleRelation(UserVO userVO){
        String userId = userVO.getUserId();
        List<String> roleIdList = userRoleMapper.selectList(new LambdaQueryWrapper<UserRoleEntity>()
                .eq(UserRoleEntity::getUserId, userId)
        ).stream().map(UserRoleEntity::getRoleId).distinct().toList();
        if(roleIdList.isEmpty()) return;
        List<RoleVO> roleVOList = roleMapper.selectList(new LambdaQueryWrapper<RoleEntity>()
                .in(RoleEntity::getRoleId, roleIdList)
        ).stream().map(it -> {
            RoleVO roleVO = new RoleVO();
            BeanUtils.copyProperties(it, roleVO);
            return roleVO;
        }).toList();
        userVO.setRoleVOList(roleVOList);
    }

    private void updateUserRoleRelation(String userId, List<RoleVO> roleVOList) {
        if (roleVOList == null) return;
        userRoleMapper.deleteByMap(Map.of("user_id", userId));
        List<UserRoleEntity> userRolelist = roleVOList.stream().map(roleVO -> {
            UserRoleEntity entity = new UserRoleEntity();
            entity.setUserId(userId);
            entity.setRoleId(roleVO.getRoleId());
            return entity;
        }).toList();
        userRoleMapper.insertOrUpdate(userRolelist);
    }

    @Override
    public PageResult<UserVO> page(UserPageDTO userPageDTO) {
        Page<?> page = PageUtil.convert2PageQuery(userPageDTO);
        List<UserVO> list = userMapper.queryPage(page, userPageDTO);
        // ---------------------- 中间表 ---------------------------
        for (UserVO userVO : list) {
            setRoleVOByUserRoleRelation(userVO);
            userVO.setPassword(null);
        }
        // ---------------------------------------------------------
        PageResult<UserVO> pageResult = PageUtil.convert2PageResult(page, list);
        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean add(UserAddDTO userAddDTO) {
        UserEntity user = new UserEntity();
        BeanUtils.copyProperties(userAddDTO, user);
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        boolean saved = save(user);
        if (!saved) {
            throw new BusinessException("添加失败");
        }
        // ----- 中间表 UserRole -----
        updateUserRoleRelation(user.getUserId(), userAddDTO.getRoleVOList());
        // -----------------
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean delete(String id) {
        UserEntity exists = getById(id);
        if (exists == null) throw new BusinessException("不存在该对象");
        // ----- 中间表 UserRole -----
        userRoleMapper.deleteByMap(Map.of("user_id", exists.getUserId()));
        // -----------------
        boolean removed = removeById(id);
        if (!removed) throw new BusinessException("删除失败");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean batchDelete(List<String> ids) {
        // ----- 中间表 UserRole -----
        listByIds(ids).forEach(entity -> userRoleMapper.deleteByMap(Map.of("user_id", entity.getUserId())));
        // -----------------
        boolean removed = removeBatchByIds(ids);
        if (!removed) throw new BusinessException("删除失败");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean update(UserUpdateDTO userUpdateDTO) {
        UserEntity user = new UserEntity();
        BeanUtils.copyProperties(userUpdateDTO, user);
        if (user.getUserId() == null) throw new BusinessException("主键不能为空");
        else {
            UserEntity exists = getById(user.getUserId());
            if (exists == null) throw new BusinessException("不存在该对象");
        }
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        boolean updated = updateById(user);
        if (!updated) {
            throw new BusinessException("更新失败");
        }
        // ----- 中间表 UserRole -----
        updateUserRoleRelation(user.getUserId(), userUpdateDTO.getRoleVOList());
        // -----------------
        return true;
    }

    @Override
    public UserVO get(String id) {
        UserEntity user = getById(id);
        if (user == null) {
            throw new BusinessException("不存在该对象");
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        setRoleVOByUserRoleRelation(userVO);
        userVO.setPassword(null);
        return userVO;
    }



    @Override
    public UserInfo getUserInfo(String username) {
        UserEntity loadedUser = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, username));
        if (loadedUser == null) throw new BusinessException("用户不存在");
        List<RoleEntity> roles = userRoleMapper.findRoleByUserId(loadedUser.getUserId());
        SortedSet<MenuEntity> menus = new TreeSet<>(Comparator
                .comparing(MenuEntity::getPriority)
                .thenComparing(MenuEntity::getMenuId));
        for (RoleEntity role : roles) {
            List<MenuEntity> menuEntityList = menuMapper.findMenuByRoleIds(
                    Collections.singletonList(role.getRoleId()));
            menus.addAll(menuEntityList);
        }
        UserInfo userInfo = new UserInfo(loadedUser, new HashSet<>(roles), menus);
        userInfo.erasePassword();
        return userInfo;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public UserVO register(RegisterDTO registerDTO) {
        if (!systemSettingService.isRegisterEnabled()) {
            throw new BusinessException("注册已关闭");
        }
        if (!registerDTO.getPassword().equals(registerDTO.getNewPassword()))
            throw new BusinessException("两次密码不一致");
        String username = registerDTO.getUsername();
        if (username == null || username.isBlank()) throw new BusinessException("用户名不能为空");
        UserEntity exist = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username), false);
        if (exist != null) throw new BusinessException("用户名已存在");
        UserEntity user = new UserEntity();
        BeanUtils.copyProperties(registerDTO, user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAvatar(pushUrl + "/default/avatar.png");
        boolean saved = save(user);
        return get(user.getUserId());
    }

    @Override
    public List<UserVO> getSameRoleUsers() {
        List<UserEntity> list = this.list();
        List<UserVO> res = new ArrayList<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo loginUserInfo = getUserInfo(authentication.getName());

        for (UserEntity userEntity : list) {
            UserInfo targetUserInfo = getUserInfo(userEntity.getUsername());

            // 判断两个用户角色集合是否有交集
            boolean hasCommonRole = false;
            for (RoleEntity role : loginUserInfo.getRoles()) {
                if (targetUserInfo.getRoles().contains(role)) {
                    hasCommonRole = true;
                    break;
                }
            }

            if (hasCommonRole) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(userEntity, userVO);
                userVO.setCreateUser(null);
                userVO.setUsername("******");
                userVO.setPassword("******");
                userVO.setUpdateUser(null);
                res.add(userVO);
            }
        }
        return res;
    }
}
