package com.example.springbootlearning.converter;

import com.example.springbootlearning.dto.UserSaveDTO;
import com.example.springbootlearning.dto.UserVO;
import com.example.springbootlearning.entity.Account;
import com.example.springbootlearning.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserConverter {

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.name", target = "name")
    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "account.balance", target = "balance")
    UserVO toVO(User user, Account account);

    @Mapping(target = "createTime", ignore = true)
    User toEntity(UserSaveDTO dto);
}
