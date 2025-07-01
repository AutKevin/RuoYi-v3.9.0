package com.ruoyi.quartz.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 定时任务调度
 * 定时同步slave的数据到master用户和组织信息
 * @author
 */
@Component("UserCitySyncTask")
public class UserCitySyncTask
{
    private final JdbcTemplate slaveJdbcTemplate;
    private final JdbcTemplate masterJdbcTemplate;

    @Autowired
    public UserCitySyncTask(@Qualifier("masterjdbcTemplate") JdbcTemplate masterJdbcTemplate,
                            @Qualifier("slavejdbcTemplate") JdbcTemplate slaveJdbcTemplate) {
        this.masterJdbcTemplate = masterJdbcTemplate;
        this.slaveJdbcTemplate = slaveJdbcTemplate;
    }

    /**
     * 同步slave数据中的user表到master数据中的sys_user、sys_user_role表
     */
    public void syncUserData() {
        // 获取slave的user表数据
        String fetchSql = "SELECT uid, username, nickname, status, role_id, city_id FROM user";
        List<Map<String, Object>> slaveUsers = slaveJdbcTemplate.queryForList(fetchSql);

        //构建master.sys_user表的插入语句
        String insertUserSql = "INSERT INTO sys_user (user_id, user_name, nick_name, dept_id) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "user_name = VALUES(user_name), " +
                "nick_name = VALUES(nick_name), " +
                "dept_id = VALUES(dept_id)";
        // 批处理sys_user的参数
        List<Object[]> userBatchParams = slaveUsers.stream()
                .map(slaveUser -> new Object[]{
                        slaveUser.get("uid"),
                        slaveUser.get("username"),
                        slaveUser.get("nickname"),
                        slaveUser.get("status"),
                        slaveUser.get("city_id")
                })
                .collect(Collectors.toList());
        masterJdbcTemplate.batchUpdate(insertUserSql, userBatchParams);

        //插入用户角色关联表
        String insertUserRoleSql = "INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)" +
                "ON DUPLICATE KEY UPDATE role_id = VALUES(role_id)";
        // 用户角色关联表
        List<Object[]> userRoleBatchParams = slaveUsers.stream()
                .filter(slaveUser -> slaveUser.get("role_id") != null && !slaveUser.get("role_id").toString().isEmpty())
                .map(slaveUser -> new Object[]{
                        slaveUser.get("uid"),
                        slaveUser.get("role_id")
                })
                .collect(Collectors.toList());
        masterJdbcTemplate.batchUpdate(insertUserRoleSql, userRoleBatchParams);
    }

    /**
     * 同步slave数据中的city表到master数据中的sys_dept表
     */
    public void syncCityData() {
        // 获取slave的表数据
        String fetchCitySql = "SELECT id, city_name FROM city";
        List<Map<String, Object>> slaveCities = slaveJdbcTemplate.queryForList(fetchCitySql);

        // 构建master.sys_dept表的插入语句
        String insertCitySql = "INSERT INTO sys_dept (dept_id, dept_name, parent_id) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "dept_name = VALUES(dept_name)";

        // 批处理sys_dept的参数
        List<Object[]> cityBatchParams = slaveCities.stream()
                .map(slaveCity -> new Object[]{
                        slaveCity.get("id"),
                        slaveCity.get("city_name"),
                        200000 // 固定的parent_id
                })
                .collect(Collectors.toList());

        // 执行批量插入或更新
        masterJdbcTemplate.batchUpdate(insertCitySql, cityBatchParams);
    }

}
