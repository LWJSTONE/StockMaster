package com.stockmaster.common.config;

import com.stockmaster.common.enums.StockStatus;
import com.stockmaster.modules.stock.entity.Category;
import com.stockmaster.modules.stock.repository.CategoryRepository;
import com.stockmaster.modules.system.entity.Menu;
import com.stockmaster.modules.system.entity.Role;
import com.stockmaster.modules.system.entity.RoleMenu;
import com.stockmaster.modules.system.entity.User;
import com.stockmaster.modules.system.entity.UserRole;
import com.stockmaster.modules.system.repository.MenuRepository;
import com.stockmaster.modules.system.repository.RoleMenuRepository;
import com.stockmaster.modules.system.repository.RoleRepository;
import com.stockmaster.modules.system.repository.UserRepository;
import com.stockmaster.modules.system.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("开始初始化系统数据...");
        
        initRoles();
        initMenus();
        initAdminUser();
        initCategories();
        
        log.info("系统数据初始化完成");
    }

    private void initRoles() {
        if (roleRepository.count() == 0) {
            List<Role> roles = new ArrayList<>();
            
            Role adminRole = new Role();
            adminRole.setRoleCode("ADMIN");
            adminRole.setRoleName("系统管理员");
            adminRole.setDescription("拥有所有权限");
            adminRole.setSortOrder(1);
            adminRole.setStatus(1);
            roles.add(adminRole);

            Role managerRole = new Role();
            managerRole.setRoleCode("MANAGER");
            managerRole.setRoleName("仓库管理员");
            managerRole.setDescription("管理库存和商品");
            managerRole.setSortOrder(2);
            managerRole.setStatus(1);
            roles.add(managerRole);

            Role userRole = new Role();
            userRole.setRoleCode("USER");
            userRole.setRoleName("普通用户");
            userRole.setDescription("基本操作权限");
            userRole.setSortOrder(3);
            userRole.setStatus(1);
            roles.add(userRole);

            roleRepository.saveAll(roles);
            log.info("初始化角色数据完成");
        }
    }

    private void initMenus() {
        if (menuRepository.count() == 0) {
            List<Menu> menus = new ArrayList<>();
            
            // 仪表盘
            Menu dashboard = createMenu(1L, null, "仪表盘", "/dashboard", "dashboard/index", "dashboard:view", "DashboardOutlined", 1, 1);
            menus.add(dashboard);

            // 商品管理
            Menu product = createMenu(2L, null, "商品管理", "/product", null, null, "ShoppingOutlined", 1, 2);
            menus.add(product);
            menus.add(createMenu(3L, 2L, "商品列表", "/product/list", "product/list", "product:list", null, 1, 1));
            menus.add(createMenu(4L, 2L, "商品分类", "/product/category", "product/category", "category:list", null, 1, 2));

            // 库存管理
            Menu stock = createMenu(5L, null, "库存管理", "/stock", null, null, "DatabaseOutlined", 1, 3);
            menus.add(stock);
            menus.add(createMenu(6L, 5L, "库存查询", "/stock/inventory", "stock/inventory", "inventory:list", null, 1, 1));
            menus.add(createMenu(7L, 5L, "入库管理", "/stock/inbound", "stock/inbound", "inbound:list", null, 1, 2));
            menus.add(createMenu(8L, 5L, "出库管理", "/stock/outbound", "stock/outbound", "outbound:list", null, 1, 3));

            // 采购管理
            Menu purchase = createMenu(9L, null, "采购管理", "/purchase", null, null, "ShoppingCartOutlined", 1, 4);
            menus.add(purchase);
            menus.add(createMenu(10L, 9L, "供应商管理", "/purchase/supplier", "purchase/supplier", "supplier:list", null, 1, 1));
            menus.add(createMenu(11L, 9L, "供应商评价", "/purchase/evaluation", "purchase/evaluation", "evaluation:list", null, 1, 2));
            menus.add(createMenu(12L, 9L, "采购订单", "/purchase/order", "purchase/order", "purchase-order:list", null, 1, 3));

            // 系统管理
            Menu system = createMenu(13L, null, "系统管理", "/system", null, null, "SettingOutlined", 1, 5);
            menus.add(system);
            menus.add(createMenu(14L, 13L, "用户管理", "/system/user", "system/user", "user:list", null, 1, 1));
            menus.add(createMenu(15L, 13L, "角色管理", "/system/role", "system/role", "role:list", null, 1, 2));
            menus.add(createMenu(16L, 13L, "菜单管理", "/system/menu", "system/menu", "menu:list", null, 1, 3));
            menus.add(createMenu(17L, 13L, "操作日志", "/system/log", "system/log", "log:list", null, 1, 4));

            menuRepository.saveAll(menus);
            
            // 为管理员角色分配所有菜单
            Role adminRole = roleRepository.findByRoleCode("ADMIN").orElse(null);
            if (adminRole != null) {
                List<RoleMenu> roleMenus = new ArrayList<>();
                for (Menu menu : menus) {
                    RoleMenu rm = new RoleMenu(adminRole.getId(), menu.getId());
                    roleMenus.add(rm);
                }
                roleMenuRepository.saveAll(roleMenus);
            }
            
            log.info("初始化菜单数据完成");
        }
    }

    private Menu createMenu(Long id, Long parentId, String menuName, String path, String component, String permission, String icon, Integer menuType, Integer sortOrder) {
        Menu menu = new Menu();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setMenuName(menuName);
        menu.setPath(path);
        menu.setComponent(component);
        menu.setPermission(permission);
        menu.setIcon(icon);
        menu.setMenuType(menuType);
        menu.setSortOrder(sortOrder);
        menu.setVisible(true);
        menu.setStatus(1);
        menu.setIsExternal(false);
        menu.setIsCached(false);
        return menu;
    }

    private void initAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setEmail("admin@stockmaster.com");
            admin.setPhone("13800138000");
            admin.setGender(1);
            admin.setStatus(1);
            admin.setAdmin(true);
            
            admin = userRepository.save(admin);
            
            // 分配管理员角色
            Role adminRole = roleRepository.findByRoleCode("ADMIN").orElse(null);
            if (adminRole != null) {
                UserRole userRole = new UserRole(admin.getId(), adminRole.getId());
                userRoleRepository.save(userRole);
            }
            
            log.info("初始化管理员账户完成，用户名: admin，密码: admin123");
        }
    }

    private void initCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> categories = new ArrayList<>();
            
            Category electronics = new Category();
            electronics.setCategoryName("电子产品");
            electronics.setCategoryCode("ELECTRONICS");
            electronics.setSortOrder(1);
            electronics.setStatus(1);
            categories.add(electronics);

            Category clothing = new Category();
            clothing.setCategoryName("服装鞋帽");
            clothing.setCategoryCode("CLOTHING");
            clothing.setSortOrder(2);
            clothing.setStatus(1);
            categories.add(clothing);

            Category food = new Category();
            food.setCategoryName("食品饮料");
            food.setCategoryCode("FOOD");
            food.setSortOrder(3);
            food.setStatus(1);
            categories.add(food);

            Category daily = new Category();
            daily.setCategoryName("日用百货");
            daily.setCategoryCode("DAILY");
            daily.setSortOrder(4);
            daily.setStatus(1);
            categories.add(daily);

            Category office = new Category();
            office.setCategoryName("办公用品");
            office.setCategoryCode("OFFICE");
            office.setSortOrder(5);
            office.setStatus(1);
            categories.add(office);

            categoryRepository.saveAll(categories);
            log.info("初始化商品分类数据完成");
        }
    }
}
