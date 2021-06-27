package org.smartjq.mvc.common.model;

import java.util.ArrayList;
import java.util.List;

import org.smartjq.mvc.common.dto.ZtreeNode;
import org.smartjq.mvc.common.model.base.BaseDjOrg;
import org.smartjq.mvc.common.utils.ContextUtil;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class DjOrg extends BaseDjOrg<DjOrg> {
	public static final DjOrg dao = new DjOrg();
	public static List<DjOrg> allChildren = new ArrayList<DjOrg>();
	/***
	 * 根据主键查询
	 */
	public DjOrg getById(String id){
		return DjOrg.dao.findById(id);
	}
	/***
	 * 删除
	 * @param ids
	 */
	@Before(Tx.class)
	public void deleteByIds(String ids){
    	String idarr[] = ids.split(",");
    	for(String id : idarr){
    		DjOrg o = DjOrg.dao.getById(id);
    		o.delete();
    	}
	}

	/***
	 * 根据id 查询孩子(只查一级孩子)
	 * @param id
	 * @param type  0:部门 1:公司
	 * @return
	 */
	public List<DjOrg> getChildrenByPid(String id,String type){
		String sql = "select * from dj_org m where m.parent_id='"+id+"' ";
		if(StrKit.notBlank(type)){
			sql = sql + " and m.type='"+type+"' ";
		}
		sql = sql + " order by m.sort";
		return DjOrg.dao.find(sql);
	}
	/***
	 * 递归查询，所有的的孩子（不包含自己）,树形结构
	 * @param id
	 * @return
	 */
	public List<DjOrg> getChildrenAllTree(String id){
		List<DjOrg> list =  getChildrenByPid(id,null);//根据id查询孩子
		for(DjOrg o : list){
			o.setChildren(getChildrenAllTree(o.getId()));
		}
		return list;
	}
	/***
	 * 递归查询，所有的的孩子（不包含自己）
	 * @return
	 */
	public List<DjOrg> getAllChildren(String pid){
		allChildren.clear();
		List<DjOrg> orgList = dao.findAll();
		return getChildrenAll(orgList,pid);
	}
	private List<DjOrg> getChildrenAll(List<DjOrg>  orgs ,String pid){
		for (DjOrg org : orgs) {
			// 判断是否存在孩子
			if (pid.equals(org.getParentId())) {
				// 递归遍历上一级
				getChildrenAll(orgs, org.getId());
				allChildren.add(org);
			}
		}
		return allChildren;
	}
	/***
	 * 查询同一级的子公司下的所有单位
	 * 如果传入的是子公司id。返回不包含自己的所有的部门。
	 * 如果传入的是部门id。返回的是所有同一子公司的部门(也不包含自己)。
	 */
	public List<DjOrg> getChildCompanyList(String id){
		List<DjOrg> result = new ArrayList<DjOrg>();
		DjOrg org = DjOrg.dao.getById(id);
		if(org!=null){
			if("1".equals(org.getType())){//子公司
				result = dao.find("select * from dj_org o where o.parent_child_company_id='"+org.getId()+"'");
			}else if("".equals(org.getType())){//部门
				result = dao.find("select * from dj_org o where o.parent_child_company_id='"+org.getParentChildCompanyId()+"'");
			}
		}
		return result;
	}
	/***
	 * 递归查询，所有的父亲（不包括自己）
	 */
	public List<DjOrg> getParentAll(String id){
		List<DjOrg> result = new ArrayList<DjOrg>();
		DjOrg org = DjOrg.dao.getById(id);//自己
		result = getHisParent(result,org);
		return result;
	}
	/***
	 * 递归查父级
	 * @param list
	 * @param org
	 * @return
	 */
	public List<DjOrg> getHisParent(List<DjOrg> list,DjOrg org){
		String pid = org.getParentId();
		if(StrKit.notBlank(pid)){
			DjOrg parent = DjOrg.dao.getById(org.getParentId());
			list.add(parent);
			getHisParent(list,parent);
		}
		return list;
	}
	/***
	 * 菜单转成ZTreeNode
	 * @param 
	 * olist 数据
	 * open  是否展开所有
	 * ifOnlyLeaf 是否只选叶子
	 * @return
	 */
	public List<ZtreeNode> toZTreeNode(List<DjOrg> olist,Boolean open,Boolean ifOnlyLeaf){
		List<ZtreeNode> list = new ArrayList<ZtreeNode>();
		for(DjOrg o : olist){
			ZtreeNode node = toZTreeNode(o);
			if(o.getChildren()!=null&&o.getChildren().size()>0){//如果有孩子
				node.setChildren(toZTreeNode(o.getChildren(),open,ifOnlyLeaf));
				if(ifOnlyLeaf){//如果只选叶子
					node.setNocheck(true);
				}
			}
			if("1".equals(node.getType())){
				node.setOpen(true);
			}else{
				node.setOpen(open);
			}
			list.add(node);
		}
		return list;
	}
	/***
	 * 菜单转成ZtreeNode
	 * @param 
	 * @return
	 */
	public ZtreeNode toZTreeNode(DjOrg org){
		ZtreeNode node = new ZtreeNode();
		node.setId(org.getId());
		node.setName(org.getName());
		node.setType(org.getType());
		if("1".equals(org.getType())){
			node.setIcon(ContextUtil.getCtx()+"/common/plugins/zTree_v3/css/zTreeStyle/img/diy/1_open.png");
		}
//		node.setLevel(menu.getLevel());
		return node;
	}

	/***
	 * 根据id 查询孩子分页
	 * @param pnum
	 * @param psize
	 * @param pid
	 * @return
	 */
	public Page<Record> getChildrenPageByPid(int pnum,int psize, String pid){
		String sql = " from dj_org o1 LEFT JOIN dj_org o2 on o1.parent_id=o2.id"
				+ " left join sys_dct as d1 on d1.value=o1.area "
				+ " left join sys_dct as d2 on d2.value=o1.union_type where d2.type='union_type' and d1.type='union_area' ";
		if(StrKit.notBlank(pid)){
			sql = sql + " and o1.parent_id='"+pid+"' ";
		}
		sql = sql + " order by o1.sort ";
		Page<Record> page =  Db.paginate(pnum, psize, "select o1.* , o2.name parent_name, d1.name as areaname, d2.name as typename ", sql);
		List<Record> list = page.getList();
		for(Record r : list){
			String parentCompanyId = r.getStr("parent_child_company_id");
			DjOrg o = DjOrg.dao.getById(parentCompanyId);
			if(o!=null){
				r.set("parent_child_company_name", o.getName());
			}
		}
		return page;
	}
	
	/****
	 * 获取某机构所在的一级子公司
	 */
	public DjOrg getFirstChildCompany(DjOrg org){
		DjOrg result = null;
		List<DjOrg> allList = org.getParentAll(org.getId());
		allList.add(org);//自己也有可能是子公司类型
		if(allList!=null&&allList.size()>0){
			for(DjOrg o:allList){
				if(StrKit.isBlank(o.getParentChildCompanyId())&&!"#root".equals(o.getId())&&"#root".equals(o.getParentId())){
					result = o;
				}
			}
		}
		return result;
	}
	
	/***
	 * 根据username获取组织结构
	 * @param username
	 */
	public DjOrg getByUsername(String username){
		SysUser user = SysUser.dao.getByUsername(username);
		if(user!=null){
			String orgid = user.getOrgid();
			if(StrKit.notBlank(orgid)){
				DjOrg org = DjOrg.dao.getById(orgid);
				return org;
			}
		}
		return null;
	}
	public DjOrg getByUserId(String userid){
		SysUser user = SysUser.dao.getById(userid);
		if(user!=null){
			String orgid = user.getOrgid();
			if(StrKit.notBlank(orgid)){
				DjOrg org = DjOrg.dao.getById(orgid);
				return org;
			}
		}
		return null;
	}
	
	public String getOrgNameById(String id){
		DjOrg org = DjOrg.dao.getById(id);
		if(org!=null){
			return org.getName();
		}else{
			return "";
		}
	}
	
}