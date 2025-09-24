package org.project.reggie.controller;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.project.reggie.common.BaseContext;
import org.project.reggie.common.R;
import org.project.reggie.entity.AddressBook;
import org.project.reggie.service.AddressBookService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 地址管理 前端控制器
 * </p>
 *
 * @author anyi
 * @since 2022-05-25
 */
@RestController
@Slf4j
@RequestMapping("/addressBook")
public class AddressBookController {

    @Resource
    private AddressBookService addressBookService;

    /**
     * 添加地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R add(@RequestBody AddressBook addressBook){
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        addressBookService.save(addressBook);
        return R.success("添加地址成功！");
    }

    /**
     * 查询当前用户所有地址
     * @return
     */
    @GetMapping("/list")
    public R getList(){
        Long userId = BaseContext.getCurrentId();
        List<AddressBook> list = addressBookService.list(new LambdaQueryWrapper<AddressBook>()
                .orderByDesc(AddressBook::getIsDefault)
                .eq(AddressBook::getUserId,userId));
        return R.success(list);
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R changeDefault(@RequestBody AddressBook addressBook){
        addressBookService.setDefault(addressBook);
        return R.success("默认地址设置成功!");
    }

    /**
     * 根据id获取地址
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R getAddress(@PathVariable Long id){
        AddressBook addressBook = addressBookService.getById(id);
        return R.success(addressBook);
    }

    /**
     * 获取默认地址
     * @return
     */
    @GetMapping("/default")
    public R getDefault(){
        Long userId = BaseContext.getCurrentId();
        AddressBook one = addressBookService.getOne(
                new LambdaQueryWrapper<AddressBook>()
                        .eq(AddressBook::getIsDefault, true)
                        .eq(AddressBook::getUserId,userId));
        return R.success(one);
    }
}

