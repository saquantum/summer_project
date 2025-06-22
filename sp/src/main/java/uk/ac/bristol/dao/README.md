This packages stores data-accessing interfaces which will be proxied by MyBatis. For every method in this package, you should be able to find an ORM under `resources/uk/ac/bristol/dao/`. For example, for the method `List<AssetHolder> selectAllAssetHolders()`, there is a configuration `<select id="selectAllAssetHolders" resultMap="AssetHolderMap"> select * from asset_holders </select>` that also explains its SQL statement. These methods are mostly self-documented. To invoke these methods, inject a mapper object with `@Autowired` or constructor injection:

```java
@Autowired
private UserMapper userMapper; // automatically an implementation of the interface injected by Spring.

public List<User> select(){
    return userMapper.selectAllUsers();
}
```