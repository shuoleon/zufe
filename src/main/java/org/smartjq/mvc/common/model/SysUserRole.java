package org.smartjq.mvc.common.model;

import org.smartjq.mvc.common.model.base.BaseSysUserRole;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class SysUserRole extends BaseSysUserRole<SysUserRole> {
	public static final SysUserRole dao = new SysUserRole();
	public static final String tableName = "sys_user_role";
	
	/***
	 * 查询
	 * @param userid
	 * @param roleid
	 * @return
	 */
	public SysUserRole getByUseridAndRoleid(String userid,String roleid){
		return dao.findFirst("select * from sys_user_role ur where ur.role_id='"+roleid+"' and ur.user_id='"+userid+"'");
	}
}
