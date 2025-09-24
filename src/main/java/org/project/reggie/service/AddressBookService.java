package org.project.reggie.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.project.reggie.entity.AddressBook;

/**
 * <p>
 * 地址管理 服务类
 * </p>
 *
 * @author anyi
 * @since 2022-05-25
 */
public interface AddressBookService extends IService<AddressBook> {

    void setDefault(AddressBook addressBook);
}
