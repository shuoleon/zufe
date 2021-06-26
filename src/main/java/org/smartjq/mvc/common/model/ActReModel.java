package org.smartjq.mvc.common.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jfinal.plugin.activerecord.Page;
import org.smartjq.mvc.common.model.base.BaseActReModel;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class ActReModel extends BaseActReModel<ActReModel> {
	public static final ActReModel dao = new ActReModel();
	/***
	 * 固定流程查询分页
	 * @param curr
	 * @param pagesize
	 * @return
	 */
	public Page<ActReModel> getAbsoluteModelPage(Integer curr , Integer pagesize){
		String sql = " from (SELECT MAX(VERSION_) maxversion,KEY_ FROM act_re_model WHERE ";
		List<String> list = new ArrayList<String>(); 
		List<SysDct> l = SysDct.dao.getByType("FLOW_DEFKEY_TO_MODELNAME");
		for(SysDct d : l){
			list.add(" KEY_='"+d.getKey()+"' ");
		}
		sql = sql + " ( " + StringUtils.join(list," or ") + " ) group by KEY_ ) a ,act_re_model m where m.KEY_=a.KEY_ and m.VERSION_=a.maxversion order by m.KEY_ ASC";
		return ActReModel.dao.paginate(curr, pagesize, "select * ", sql);
	}
	
	
	/***
	 * 自定义流程查询分页
	 * @param curr
	 * @param pagesize
	 * @return
	 */
	public Page<ActReModel> getCustomModelPage(Integer curr , Integer pagesize){
		String sql = " from (SELECT MAX(VERSION_) maxversion,KEY_ FROM act_re_model WHERE ";
		List<String> list = new ArrayList<String>(); 
		List<SysDct> l = SysDct.dao.getByType("FLOW_DEFKEY_TO_MODELNAME");
		for(SysDct d : l){
			list.add(" KEY_!='"+d.getKey()+"' ");
		}
		sql = sql + " ( " + StringUtils.join(list," and ") + " ) group by KEY_ ) a ,act_re_model m where m.KEY_=a.KEY_ and m.VERSION_=a.maxversion order by m.KEY_ ASC";
		return ActReModel.dao.paginate(curr, pagesize, "select * ", sql);
	}
}
