package com.example.grademanage.Service;

import com.example.grademanage.DAO.UserDAO;
import com.example.grademanage.Entity.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 用户业务层（封装业务逻辑 + 统一异常处理 + 参数校验）
 * 修正：1. userId改为String类型，适配数据库VARCHAR(30) 2. 移除所有用户名（userName）相关逻辑
 */
public class UserService {
    // 依赖注入DAO
    @Getter
    @Setter
    private UserDAO dao;

    /**
     * 新增用户
     * @param user 用户对象（必填：realName/password/roleId）
     */
    public void addUser(User user) {
        // 1. 业务参数校验（移除userName校验）
        validateUserParam(user, true);

        // 2. 移除：检查用户名重复的业务逻辑

        // 3. 调用DAO层保存
        dao.save(user);
        System.out.println("用户[" + user.getRealName() + "]新增成功");
    }

    /**
     * 删除用户
     * @param userId 用户ID（String类型）
     */
    public void deleteUser(String userId) {
        // 1. 参数校验：改为String非空校验
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("删除用户失败：用户ID不能为空");
        }

        // 2. 业务逻辑：检查用户是否存在
        User existUser = dao.findById(userId);
        if (existUser == null) {
            throw new RuntimeException("删除用户失败：ID为" + userId + "的用户不存在");
        }

        // 3. 调用DAO层删除
        dao.delete(userId);
        System.out.println("用户[" + existUser.getRealName() + "]删除成功");
    }

    /**
     * 更新用户信息
     * @param user 用户对象（必填：userId）
     */
    public void updateUser(User user) {
        // 1. 参数校验
        validateUserParam(user, false);

        // 2. 业务逻辑：检查用户是否存在
        User existUser = dao.findById(user.getUserId());
        if (existUser == null) {
            throw new RuntimeException("更新用户失败：ID为" + user.getUserId() + "的用户不存在");
        }

        // 3. 调用DAO层更新
        dao.update(user);
        System.out.println("用户[" + user.getRealName() + "]信息更新成功");
    }

    /**
     * 根据ID查询用户
     * @param userId 用户ID（String类型）
     * @return 用户对象
     */
    public User getUserById(String userId) {
        // 校验改为String非空
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("查询用户失败：用户ID不能为空");
        }
        User user = dao.findById(userId);
        if (user == null) {
            throw new RuntimeException("查询用户失败：ID为" + userId + "的用户不存在");
        }
        return user;
    }

    /**
     * 根据姓名模糊查询用户
     * @param realName 真实姓名
     * @return 用户列表
     */
    public List<User> getUserByRealName(String realName) {
        if (realName == null || realName.trim().isEmpty()) {
            throw new IllegalArgumentException("查询用户失败：姓名不能为空");
        }
        List<User> userList = dao.findByRealName(realName);
        if (userList.isEmpty()) {
            System.out.println("查询提示：未找到姓名包含[" + realName + "]的用户");
        }
        return userList;
    }

    /**
     * 查询所有用户
     * @return 所有用户列表
     */
    public List<User> getAllUsers() {
        List<User> userList = dao.findAll();
        if (userList.isEmpty()) {
            System.out.println("查询提示：暂无用户数据");
        }
        return userList;
    }

    /**
     * 私有工具方法：用户参数校验（移除userName相关校验）
     * @param user 用户对象
     * @param isAdd 是否为新增操作（新增需校验更多必填项）
     */
    private void validateUserParam(User user, boolean isAdd) {
        if (user == null) {
            throw new IllegalArgumentException("用户对象不能为null");
        }
        // 新增操作必填项（移除userName校验）
        if (isAdd) {
            if (user.getRealName() == null || user.getRealName().trim().isEmpty()) {
                throw new IllegalArgumentException("新增用户失败：真实姓名不能为空");
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                throw new IllegalArgumentException("新增用户失败：密码不能为空");
            }
            if (user.getRoleId() == null) { // role_id仍为Integer（根据数据库结构）
                throw new IllegalArgumentException("新增用户失败：角色ID不能为空");
            }
        } else {
            // 更新操作必填项：改为String类型校验
            if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
                throw new IllegalArgumentException("更新用户失败：用户ID不能为空");
            }
        }
    }
}