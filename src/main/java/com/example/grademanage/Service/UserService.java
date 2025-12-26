package com.example.grademanage.Service;

import com.example.grademanage.DAO.UserDAO;
import com.example.grademanage.Entity.User;
import com.example.grademanage.Util.ValiDateUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/**
 * 用户业务层
 */
@Getter
@Setter
public class UserService {
    // 依赖注入DAO
    private UserDAO dao;

    /**
     * 新增用户
     * @param user 用户对象（必填：realName/password/roleId）
     */
    public void saveUser(User user) {
        // 业务参数校验：不通过则返回
        try {
            ValiDateUtil.validateUserParam(user, true);
        } catch (IllegalArgumentException e) {
            System.out.println("新增用户失败：" + e.getMessage());
            return;
        }

        // 检查用户ID是否重复：已存在则返回
        if (dao.findById(user.getUserId()) != null) {
            System.out.println("新增用户失败：ID为" + user.getUserId() + "的用户已存在");
            return;
        }

        // 调用DAO层保存
        dao.save(user);
        System.out.println("用户[" + user.getRealName() + "]新增成功");
    }

    /**
     * 删除用户（校验不通过直接返回）
     * @param userId 用户ID
     */
    public void deleteUser(String userId) {
        // 参数校验：ID为空则返回
        if (userId == null || userId.trim().isEmpty()) {
            System.out.println("删除用户失败：用户ID不能为空");
            return;
        }

        // 业务逻辑：用户不存在则返回
        User existUser = dao.findById(userId);
        if (existUser == null) {
            System.out.println("删除用户失败：ID为" + userId + "的用户不存在");
            return;
        }

        // 调用DAO层删除
        dao.delete(userId);
        System.out.println("ID为" + userId + "的用户删除成功");
    }

    /**
     * 更新用户信息（校验不通过直接返回）
     * @param user 用户对象
     */
    public void updateUser(User user) {
        // 参数校验：不通过则返回
        try {
            ValiDateUtil.validateUserParam(user, false);
        } catch (IllegalArgumentException e) {
            System.out.println("更新用户失败：" + e.getMessage());
            return;
        }

        // 业务逻辑：用户不存在则返回
        User existUser = dao.findById(user.getUserId());
        if (existUser == null) {
            System.out.println("更新用户失败：ID为" + user.getUserId() + "的用户不存在");
            return;
        }

        // 调用DAO层更新
        dao.update(user);
        System.out.println("用户[" + user.getRealName() + "]信息更新成功");
    }

    /**
     * @param userId 用户ID（String类型）
     * @return 存在则返回用户对象，否则返回null
     */
    public User getUserById(String userId) {
        // 严格校验：ID为空/空白字符直接返回null
        if (userId == null || userId.trim().isEmpty()) {
            System.out.println("查询用户失败：用户ID不能为空或仅含空白字符");
            return null;
        }

        // 查询用户：不存在返回null
        User user = dao.findById(userId);
        if (user == null) {
            System.out.println("查询用户失败：ID为" + userId + "的用户不存在");
        }
        return user;
    }

    /**
     * @param realName 真实姓名
     * @return 存在则返回用户列表，否则返回空列表
     */
    public List<User> getUserByRealName(String realName) {
        // 校验：姓名为空返回空列表
        if (realName == null || realName.trim().isEmpty()) {
            System.out.println("查询用户失败：姓名不能为空");
            return Collections.emptyList();
        }

        // 执行查询
        List<User> userList = dao.findByRealName(realName);
        if (userList.isEmpty()) {
            System.out.println("查询提示：未找到姓名包含[" + realName + "]的用户");
        }
        return userList;
    }

    /**
     * 查询所有用户
     * @return 所有用户列表（无数据返回空列表）
     */
    public List<User> getAllUsers() {
        List<User> userList = dao.findAll();
        if (userList.isEmpty()) {
            System.out.println("查询提示：暂无用户数据");
        }
        return userList;
    }
}