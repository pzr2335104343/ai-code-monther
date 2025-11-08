package com.rong.rongaicodemother.model.enums;

import lombok.Getter;

@Getter
public enum UserRoleEnum {

    ADMIN("管理员", "admin"),
    USER("普通用户", "user");
    private final String key;
    private final String value;

    UserRoleEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static UserRoleEnum getEnumByValue(String value) {
        for (UserRoleEnum role : UserRoleEnum.values()) {
            if (role.getValue().equals(value)) {
                return role;
            }
        }
        return null;
    }

}
