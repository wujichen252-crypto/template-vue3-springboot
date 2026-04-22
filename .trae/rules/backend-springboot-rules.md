# 后端开发规范 (Spring Boot 3.2 + Java 17)

## 技术栈

- Java 17 (LTS)
- Spring Boot 3.2
- Spring Security 6
- MyBatis-Plus 3.5
- MySQL 8
- Redis 7
- JWT (jjwt 0.12)
- Maven

## 项目结构

```
backend/src/main/java/com/template/
├── config/              # 配置类
├── controller/          # 控制层
├── service/            # 业务层
│   └── impl/           # 实现
├── mapper/             # 数据访问层
├── entity/             # 实体类
├── dto/                # 数据传输对象
├── vo/                 # 视图对象
├── common/             # 公共模块
│   ├── result/        # 统一响应
│   ├── exception/      # 异常处理
│   ├── constants/      # 常量
│   └── util/           # 工具类
└── interceptor/        # 过滤器/拦截器
```

## 分层职责

### Controller 层
- 处理请求参数校验
- 调用 Service 层
- 返回统一响应 `ApiResult`
- **禁止业务逻辑**

### Service 层
- 业务逻辑处理
- 事务管理
- 调用 Mapper 层
- 返回 VO/DTO

### Mapper 层
- 数据库操作
- 使用 MyBatis-Plus BaseMapper
- 复杂 SQL 使用 XML 或注解

## 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | PascalCase | `UserController` |
| 方法名 | camelCase | `getUserById` |
| 常量 | UPPER_SNAKE_CASE | `MAX_PAGE_SIZE` |
| 包名 | 全小写 | `com.template.controller` |
| 数据库表 | 下划线命名 | `user_info` |
| 字段 | 下划线命名 | `user_name` → `userName` |

## 代码规范

### Controller 规范

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ApiResult<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ApiResult.success(user);
    }

    @PostMapping
    public ApiResult<Void> createUser(@RequestBody @Valid CreateRequest request) {
        userService.createUser(request);
        return ApiResult.success();
    }
}
```

### Service 规范

```java
public interface UserService {
    UserResponse getUserById(Long id);
}

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return toUserResponse(user);
    }
}
```

### Entity 规范

```java
@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    @TableField("password_hash")
    private String passwordHash;

    private String email;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deletedAt;
}
```

### DTO/VO 规范

```java
@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
}
```

## 统一响应格式

```java
@Data
public class ApiResult<T> {
    private Integer code;
    private T data;
    private String msg;
    private String requestId;

    public static <T> ApiResult<T> success(T data) {
        // ...
    }

    public static <T> ApiResult<T> error(Integer code, String msg) {
        // ...
    }
}
```

## 异常处理

```java
// 业务异常
throw new BusinessException(ResultCode.NOT_FOUND);

// 全局捕获
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ApiResult<Void> handleBusiness(BusinessException e) {
        return ApiResult.error(e.getCode(), e.getMsg());
    }
}
```

## 数据库规范

### 表结构

- 必须包含 `id`, `created_at`, `updated_at`, `deleted_at` 字段
- 使用 InnoDB 引擎
- 字符集 utf8mb4
- 必须加注释

### 自动填充

```yaml
mybatis-plus:
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deletedAt
```

## 安全规范

- 密码必须 BCrypt 加密存储
- JWT Access Token 有效期 2 小时
- Refresh Token 有效期 7 天
- 敏感接口需要认证

## 禁止事项

- ❌ 禁止 `interface{}`，使用具体类型
- ❌ Controller 禁止业务逻辑
- ❌ 禁止硬编码错误码
- ❌ 函数不超过 50 行
- ❌ 不写 JavaDoc 注释

## Maven 命令

```bash
# 编译
mvn clean install

# 开发启动
mvn spring-boot:run

# 生产构建
mvn clean package -DskipTests

# 运行
java -jar target/template-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```
