package ru.cs_consult.datamodelbrowseaddon.security;

import io.jmix.security.role.annotation.*;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(name = "DataModelBrowseUserRole", code = DataModelBrowseUserRole.CODE)
public interface DataModelBrowseUserRole {
    String CODE = "data-model-browse-user-role";

    @ScreenPolicy( screenIds = { "dmb_DataModelBrowseScreen" })
    @MenuPolicy( menuIds = { "dmb_DataModelBrowseScreen" })

    void dataModelBrowseScreenUser();
}