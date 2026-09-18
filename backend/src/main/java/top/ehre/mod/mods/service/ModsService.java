package top.ehre.mod.mods.service;

import top.ehre.mod.mods.domain.entity.ModsEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.extension.service.IService;
import top.ehre.mod.mods.domain.vo.ModsVO;
import top.ehre.mod.mods.domain.vo.ModsCountVO;
import top.ehre.mod.mods.domain.dto.ModsPageDTO;
import top.ehre.mod.mods.domain.dto.ModsAddDTO;
import top.ehre.mod.mods.domain.dto.ModsUpdateDTO;
import top.ehre.mod.util.PageResult;

import java.util.List;


/**
 *  服务类
 *
 * @author LibrhHp_0928
 * @since 2025-11-24 18:58:33
 */
public interface ModsService extends IService<ModsEntity> {

    PageResult<ModsVO> page(ModsPageDTO modsPageDTO);

    boolean add(ModsAddDTO modsAddDTO);

    boolean delete(String id);

    boolean batchDelete(List<String> ids);

    boolean update(ModsUpdateDTO modsUpdateDTO);

    List<ModsVO> getList();

    List<ModsVO> getListByCategory(String categoryId);

    ModsVO get(String id);

    int addOtherAuthor(String id, String authorId);

    ModsCountVO incrementDownloadCount(String id);

    ModsCountVO incrementViewCount(String id);
}
