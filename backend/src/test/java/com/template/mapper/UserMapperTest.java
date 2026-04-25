package com.template.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.template.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("用户 Mapper 单元测试")
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("插入用户成功")
    void insert_Success() {
        User user = createTestUser("testuser", "test@example.com");

        int result = userMapper.insert(user);

        assertThat(result).isEqualTo(1);
        assertThat(user.getId()).isNotNull();
    }

    @Test
    @DisplayName("根据 ID 查询用户")
    void selectById_Success() {
        User user = createTestUser("testuser", "test@example.com");
        userMapper.insert(user);

        User found = userMapper.selectById(user.getId());

        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("testuser");
        assertThat(found.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("根据用户名查询用户")
    void selectByUsername_Success() {
        User user = createTestUser("uniqueuser", "unique@example.com");
        userMapper.insert(user);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, "uniqueuser");
        User found = userMapper.selectOne(wrapper);

        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("uniqueuser");
    }

    @Test
    @DisplayName("统计用户数量")
    void selectCount_Success() {
        long initialCount = userMapper.selectCount(null);

        User user = createTestUser("countuser", "count@example.com");
        userMapper.insert(user);

        long newCount = userMapper.selectCount(null);
        assertThat(newCount).isEqualTo(initialCount + 1);
    }

    @Test
    @DisplayName("更新用户信息")
    void update_Success() {
        User user = createTestUser("updateuser", "update@example.com");
        userMapper.insert(user);

        user.setEmail("newemail@example.com");
        int result = userMapper.updateById(user);

        assertThat(result).isEqualTo(1);

        User updated = userMapper.selectById(user.getId());
        assertThat(updated.getEmail()).isEqualTo("newemail@example.com");
    }

    @Test
    @DisplayName("逻辑删除用户")
    void deleteById_LogicDelete() {
        User user = createTestUser("deleteuser", "delete@example.com");
        userMapper.insert(user);
        Long userId = user.getId();

        int result = userMapper.deleteById(userId);
        assertThat(result).isEqualTo(1);

        User deleted = userMapper.selectById(userId);
        assertThat(deleted).isNull();
    }

    @Test
    @DisplayName("查询所有用户")
    void selectList_Success() {
        for (int i = 0; i < 3; i++) {
            User user = createTestUser("listuser" + i, "list" + i + "@example.com");
            userMapper.insert(user);
        }

        List<User> users = userMapper.selectList(null);

        assertThat(users).isNotEmpty();
    }

    @Test
    @DisplayName("条件查询 - 根据状态")
    void selectByCondition_Status() {
        User activeUser = createTestUser("activeuser", "active@example.com");
        userMapper.insert(activeUser);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, 1);
        List<User> activeUsers = userMapper.selectList(wrapper);

        assertThat(activeUsers).allMatch(u -> u.getStatus() == 1);
    }

    private User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("encodedPassword");
        user.setEmail(email);
        user.setStatus(1);
        return user;
    }
}
