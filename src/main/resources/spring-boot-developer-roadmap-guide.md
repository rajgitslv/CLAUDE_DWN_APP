# Java Spring Boot Developer Roadmap - Complete Interview Preparation Guide

## Senior Staff Java Engineer & Java Architect Level

---

## 1. Core Java

### Object-Oriented Programming (OOP)

**Key Concepts:**
- Encapsulation, Inheritance, Polymorphism, Abstraction
- SOLID Principles
- Design Patterns

**Example: Demonstrating SOLID Principles**

```java
// Single Responsibility Principle
public class UserService {
    private UserRepository userRepository;
    private EmailService emailService;
    
    public void registerUser(User user) {
        userRepository.save(user);
        emailService.sendWelcomeEmail(user.getEmail());
    }
}

// Open/Closed Principle - Open for extension, closed for modification
public interface PaymentProcessor {
    void processPayment(Payment payment);
}

public class CreditCardProcessor implements PaymentProcessor {
    @Override
    public void processPayment(Payment payment) {
        // Credit card specific logic
    }
}

public class PayPalProcessor implements PaymentProcessor {
    @Override
    public void processPayment(Payment payment) {
        // PayPal specific logic
    }
}

// Liskov Substitution Principle
public class Rectangle {
    protected int width;
    protected int height;
    
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public int getArea() { return width * height; }
}

// Interface Segregation Principle
public interface Workable {
    void work();
}

public interface Eatable {
    void eat();
}

public class Human implements Workable, Eatable {
    @Override
    public void work() { /* implementation */ }
    
    @Override
    public void eat() { /* implementation */ }
}

public class Robot implements Workable {
    @Override
    public void work() { /* implementation */ }
    // Robot doesn't need to implement eat()
}

// Dependency Inversion Principle
public interface NotificationService {
    void sendNotification(String message);
}

public class EmailNotificationService implements NotificationService {
    @Override
    public void sendNotification(String message) {
        // Send email
    }
}

public class OrderService {
    private final NotificationService notificationService;
    
    // Depend on abstraction, not concrete implementation
    public OrderService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    public void placeOrder(Order order) {
        // Place order logic
        notificationService.sendNotification("Order placed successfully");
    }
}
```

### Collections Framework

**Key Concepts:**
- List, Set, Map interfaces and implementations
- Performance characteristics
- Thread-safe collections
- Custom implementations

```java
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class CollectionsDemo {
    
    // ArrayList vs LinkedList
    public void demonstrateListPerformance() {
        List<String> arrayList = new ArrayList<>();
        List<String> linkedList = new LinkedList<>();
        
        // ArrayList: O(1) random access, O(n) insertion at beginning
        // LinkedList: O(n) random access, O(1) insertion at beginning
        
        arrayList.add("element"); // O(1) amortized
        linkedList.addFirst("element"); // O(1)
    }
    
    // HashSet vs TreeSet vs LinkedHashSet
    public void demonstrateSetTypes() {
        // HashSet: O(1) operations, no order
        Set<String> hashSet = new HashSet<>();
        
        // TreeSet: O(log n) operations, sorted order
        Set<String> treeSet = new TreeSet<>();
        
        // LinkedHashSet: O(1) operations, insertion order
        Set<String> linkedHashSet = new LinkedHashSet<>();
    }
    
    // HashMap vs TreeMap vs ConcurrentHashMap
    public void demonstrateMapTypes() {
        // HashMap: O(1) operations, not thread-safe
        Map<String, Integer> hashMap = new HashMap<>();
        
        // TreeMap: O(log n) operations, sorted by key
        Map<String, Integer> treeMap = new TreeMap<>();
        
        // ConcurrentHashMap: Thread-safe, better than Hashtable
        Map<String, Integer> concurrentMap = new ConcurrentHashMap<>();
    }
    
    // Custom comparable implementation
    public static class Employee implements Comparable<Employee> {
        private String name;
        private int age;
        private double salary;
        
        public Employee(String name, int age, double salary) {
            this.name = name;
            this.age = age;
            this.salary = salary;
        }
        
        @Override
        public int compareTo(Employee other) {
            return Integer.compare(this.age, other.age);
        }
        
        // Custom comparator
        public static Comparator<Employee> bySalary() {
            return Comparator.comparingDouble(Employee::getSalary);
        }
        
        public String getName() { return name; }
        public int getAge() { return age; }
        public double getSalary() { return salary; }
    }
    
    // Advanced collection operations
    public void advancedCollectionOperations() {
        List<Employee> employees = Arrays.asList(
            new Employee("John", 30, 50000),
            new Employee("Jane", 25, 60000),
            new Employee("Bob", 35, 55000)
        );
        
        // Sorting with natural order
        Collections.sort(employees);
        
        // Sorting with custom comparator
        employees.sort(Employee.bySalary());
        
        // Grouping
        Map<Integer, List<Employee>> byAge = employees.stream()
            .collect(Collectors.groupingBy(Employee::getAge));
        
        // Partitioning
        Map<Boolean, List<Employee>> partitioned = employees.stream()
            .collect(Collectors.partitioningBy(e -> e.getSalary() > 55000));
    }
}
```

### Streams API

**Key Concepts:**
- Functional programming in Java
- Stream operations (intermediate vs terminal)
- Parallel streams
- Custom collectors

```java
import java.util.*;
import java.util.stream.*;

public class StreamsDemo {
    
    public static class Transaction {
        private String id;
        private double amount;
        private String type;
        private String currency;
        
        public Transaction(String id, double amount, String type, String currency) {
            this.id = id;
            this.amount = amount;
            this.type = type;
            this.currency = currency;
        }
        
        public String getId() { return id; }
        public double getAmount() { return amount; }
        public String getType() { return type; }
        public String getCurrency() { return currency; }
    }
    
    public void demonstrateStreamOperations() {
        List<Transaction> transactions = Arrays.asList(
            new Transaction("T1", 1000, "CREDIT", "USD"),
            new Transaction("T2", 500, "DEBIT", "EUR"),
            new Transaction("T3", 1500, "CREDIT", "USD"),
            new Transaction("T4", 800, "DEBIT", "USD")
        );
        
        // Filter, Map, Reduce
        double totalCredits = transactions.stream()
            .filter(t -> "CREDIT".equals(t.getType()))
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        // Grouping and collecting
        Map<String, Double> totalByType = transactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::getType,
                Collectors.summingDouble(Transaction::getAmount)
            ));
        
        // Complex grouping
        Map<String, Map<String, List<Transaction>>> groupedByCurrencyAndType = 
            transactions.stream()
                .collect(Collectors.groupingBy(
                    Transaction::getCurrency,
                    Collectors.groupingBy(Transaction::getType)
                ));
        
        // Flat map example
        List<List<String>> nestedList = Arrays.asList(
            Arrays.asList("a", "b"),
            Arrays.asList("c", "d")
        );
        
        List<String> flatList = nestedList.stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        
        // Custom collector
        String concatenated = transactions.stream()
            .map(Transaction::getId)
            .collect(Collectors.joining(", ", "[", "]"));
        
        // Parallel streams
        double parallelSum = transactions.parallelStream()
            .mapToDouble(Transaction::getAmount)
            .sum();
    }
    
    // Advanced stream operations
    public void advancedStreamOperations() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        // takeWhile and dropWhile (Java 9+)
        List<Integer> taken = numbers.stream()
            .takeWhile(n -> n < 5)
            .collect(Collectors.toList()); // [1, 2, 3, 4]
        
        List<Integer> dropped = numbers.stream()
            .dropWhile(n -> n < 5)
            .collect(Collectors.toList()); // [5, 6, 7, 8, 9, 10]
        
        // Peek for debugging
        List<Integer> processed = numbers.stream()
            .peek(n -> System.out.println("Processing: " + n))
            .map(n -> n * 2)
            .peek(n -> System.out.println("After map: " + n))
            .collect(Collectors.toList());
        
        // Statistics
        IntSummaryStatistics stats = numbers.stream()
            .mapToInt(Integer::intValue)
            .summaryStatistics();
        
        System.out.println("Count: " + stats.getCount());
        System.out.println("Sum: " + stats.getSum());
        System.out.println("Min: " + stats.getMin());
        System.out.println("Max: " + stats.getMax());
        System.out.println("Average: " + stats.getAverage());
    }
    
    // Custom collector implementation
    public static class CustomCollector implements Collector<Transaction, 
            Map<String, Double>, Map<String, Double>> {
        
        @Override
        public Supplier<Map<String, Double>> supplier() {
            return HashMap::new;
        }
        
        @Override
        public BiConsumer<Map<String, Double>, Transaction> accumulator() {
            return (map, transaction) -> 
                map.merge(transaction.getCurrency(), 
                         transaction.getAmount(), 
                         Double::sum);
        }
        
        @Override
        public BinaryOperator<Map<String, Double>> combiner() {
            return (map1, map2) -> {
                map2.forEach((key, value) -> 
                    map1.merge(key, value, Double::sum));
                return map1;
            };
        }
        
        @Override
        public Function<Map<String, Double>, Map<String, Double>> finisher() {
            return Function.identity();
        }
        
        @Override
        public Set<Characteristics> characteristics() {
            return Collections.singleton(Characteristics.IDENTITY_FINISH);
        }
    }
}
```

### Lambda Expressions

**Key Concepts:**
- Functional interfaces
- Method references
- Closures and variable capture
- Lambda best practices

```java
import java.util.*;
import java.util.function.*;

public class LambdaDemo {
    
    // Functional interface
    @FunctionalInterface
    public interface Calculator {
        int calculate(int a, int b);
        
        // Default method allowed
        default int square(int x) {
            return x * x;
        }
        
        // Static method allowed
        static int cube(int x) {
            return x * x * x;
        }
    }
    
    public void demonstrateLambdas() {
        // Basic lambda
        Calculator add = (a, b) -> a + b;
        Calculator multiply = (a, b) -> a * b;
        
        System.out.println(add.calculate(5, 3)); // 8
        System.out.println(multiply.calculate(5, 3)); // 15
        
        // Built-in functional interfaces
        Predicate<String> isEmpty = String::isEmpty;
        Function<String, Integer> stringLength = String::length;
        Consumer<String> printer = System.out::println;
        Supplier<UUID> uuidGenerator = UUID::randomUUID;
        
        // BiFunction example
        BiFunction<Integer, Integer, Integer> power = (base, exponent) -> {
            int result = 1;
            for (int i = 0; i < exponent; i++) {
                result *= base;
            }
            return result;
        };
        
        // Method references
        List<String> names = Arrays.asList("John", "Jane", "Bob");
        
        // Static method reference
        names.forEach(System.out::println);
        
        // Instance method reference
        names.sort(String::compareToIgnoreCase);
        
        // Constructor reference
        Supplier<List<String>> listFactory = ArrayList::new;
        List<String> newList = listFactory.get();
    }
    
    // Variable capture (effectively final)
    public void demonstrateVariableCapture() {
        int multiplier = 10; // Must be effectively final
        
        Function<Integer, Integer> multiply = x -> x * multiplier;
        
        System.out.println(multiply.apply(5)); // 50
        
        // multiplier = 20; // This would cause compilation error
    }
    
    // Higher-order functions
    public Function<Integer, Integer> createMultiplier(int factor) {
        return x -> x * factor;
    }
    
    public void useHigherOrderFunctions() {
        Function<Integer, Integer> doubler = createMultiplier(2);
        Function<Integer, Integer> tripler = createMultiplier(3);
        
        System.out.println(doubler.apply(5)); // 10
        System.out.println(tripler.apply(5)); // 15
        
        // Function composition
        Function<Integer, Integer> addOne = x -> x + 1;
        Function<Integer, Integer> doubleIt = x -> x * 2;
        
        Function<Integer, Integer> addOneThenDouble = addOne.andThen(doubleIt);
        Function<Integer, Integer> doubleThenAddOne = addOne.compose(doubleIt);
        
        System.out.println(addOneThenDouble.apply(3)); // (3 + 1) * 2 = 8
        System.out.println(doubleThenAddOne.apply(3)); // (3 * 2) + 1 = 7
    }
    
    // Custom functional interfaces for business logic
    @FunctionalInterface
    public interface ValidationRule<T> {
        boolean validate(T value);
        
        default ValidationRule<T> and(ValidationRule<T> other) {
            return value -> this.validate(value) && other.validate(value);
        }
        
        default ValidationRule<T> or(ValidationRule<T> other) {
            return value -> this.validate(value) || other.validate(value);
        }
    }
    
    public void demonstrateValidationRules() {
        ValidationRule<String> notEmpty = s -> s != null && !s.isEmpty();
        ValidationRule<String> minLength = s -> s.length() >= 5;
        ValidationRule<String> maxLength = s -> s.length() <= 20;
        
        ValidationRule<String> combinedRule = notEmpty
            .and(minLength)
            .and(maxLength);
        
        System.out.println(combinedRule.validate("Hello")); // true
        System.out.println(combinedRule.validate("Hi")); // false
    }
}
```

---

## 2. Spring Framework Basics

### Inversion of Control (IoC)

**Key Concepts:**
- Dependency Injection
- Bean lifecycle
- ApplicationContext
- Bean scopes

```java
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import javax.annotation.*;

// Configuration class
@Configuration
@ComponentScan(basePackages = "com.example")
public class AppConfig {
    
    // Bean definition
    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
        dataSource.setUsername("user");
        dataSource.setPassword("password");
        return dataSource;
    }
    
    @Bean
    public TransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
    
    // Conditional bean creation
    @Bean
    @Profile("dev")
    public CacheManager devCacheManager() {
        return new SimpleCacheManager();
    }
    
    @Bean
    @Profile("prod")
    public CacheManager prodCacheManager() {
        return new RedisCacheManager();
    }
}

// Component with dependencies
@Component
public class OrderService {
    
    // Constructor injection (recommended)
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    
    @Autowired
    public OrderService(OrderRepository orderRepository,
                       PaymentService paymentService,
                       NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
    }
    
    public Order createOrder(OrderRequest request) {
        Order order = new Order(request);
        orderRepository.save(order);
        paymentService.processPayment(order);
        notificationService.sendConfirmation(order);
        return order;
    }
}

// Setter injection (less common)
@Component
public class UserService {
    private UserRepository userRepository;
    
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

// Field injection (not recommended for testability)
@Component
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
}
```

### Dependency Injection

**Advanced DI Patterns:**

```java
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import java.util.*;

// Qualifier for multiple implementations
public interface NotificationService {
    void sendNotification(String message);
}

@Component
@Qualifier("email")
public class EmailNotificationService implements NotificationService {
    @Override
    public void sendNotification(String message) {
        // Send email
    }
}

@Component
@Qualifier("sms")
public class SmsNotificationService implements NotificationService {
    @Override
    public void sendNotification(String message) {
        // Send SMS
    }
}

@Component
public class NotificationManager {
    
    // Inject specific implementation
    @Autowired
    @Qualifier("email")
    private NotificationService emailService;
    
    // Inject all implementations
    @Autowired
    private List<NotificationService> allServices;
    
    // Inject map of implementations
    @Autowired
    private Map<String, NotificationService> serviceMap;
    
    public void notifyAll(String message) {
        allServices.forEach(service -> service.sendNotification(message));
    }
}

// Primary bean
@Component
@Primary
public class DefaultNotificationService implements NotificationService {
    @Override
    public void sendNotification(String message) {
        // Default implementation
    }
}

// Conditional beans
@Component
@Conditional(OnDatabaseCondition.class)
public class DatabaseAuditService implements AuditService {
    // Only created if condition is met
}

public class OnDatabaseCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, 
                          AnnotatedTypeMetadata metadata) {
        return context.getEnvironment()
            .containsProperty("database.enabled");
    }
}

// Optional dependencies
@Component
public class CacheableUserService {
    
    private final UserRepository userRepository;
    private final Optional<CacheManager> cacheManager;
    
    @Autowired
    public CacheableUserService(UserRepository userRepository,
                               @Autowired(required = false) 
                               Optional<CacheManager> cacheManager) {
        this.userRepository = userRepository;
        this.cacheManager = cacheManager;
    }
    
    public User getUser(Long id) {
        if (cacheManager.isPresent()) {
            return cacheManager.get().get(id, () -> userRepository.findById(id));
        }
        return userRepository.findById(id);
    }
}
```

### Bean Lifecycle

**Understanding Bean Creation and Destruction:**

```java
import org.springframework.beans.factory.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;
import javax.annotation.*;

@Component
public class LifecycleBean implements InitializingBean, DisposableBean, 
        BeanNameAware, BeanFactoryAware, ApplicationContextAware {
    
    private String beanName;
    private BeanFactory beanFactory;
    private ApplicationContext applicationContext;
    
    // 1. Constructor
    public LifecycleBean() {
        System.out.println("1. Constructor called");
    }
    
    // 2. Set bean properties (dependency injection happens here)
    
    // 3. BeanNameAware
    @Override
    public void setBeanName(String name) {
        System.out.println("3. BeanNameAware: " + name);
        this.beanName = name;
    }
    
    // 4. BeanFactoryAware
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        System.out.println("4. BeanFactoryAware");
        this.beanFactory = beanFactory;
    }
    
    // 5. ApplicationContextAware
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        System.out.println("5. ApplicationContextAware");
        this.applicationContext = applicationContext;
    }
    
    // 6. @PostConstruct
    @PostConstruct
    public void postConstruct() {
        System.out.println("6. @PostConstruct");
    }
    
    // 7. InitializingBean
    @Override
    public void afterPropertiesSet() {
        System.out.println("7. InitializingBean.afterPropertiesSet()");
    }
    
    // 8. Custom init method (if defined in @Bean)
    public void customInit() {
        System.out.println("8. Custom init method");
    }
    
    // Bean is now fully initialized and ready to use
    
    // 9. @PreDestroy (on shutdown)
    @PreDestroy
    public void preDestroy() {
        System.out.println("9. @PreDestroy");
    }
    
    // 10. DisposableBean (on shutdown)
    @Override
    public void destroy() {
        System.out.println("10. DisposableBean.destroy()");
    }
    
    // 11. Custom destroy method (if defined in @Bean)
    public void customDestroy() {
        System.out.println("11. Custom destroy method");
    }
}

// Configuration with init and destroy methods
@Configuration
public class LifecycleConfig {
    
    @Bean(initMethod = "customInit", destroyMethod = "customDestroy")
    public LifecycleBean lifecycleBean() {
        return new LifecycleBean();
    }
}

// BeanPostProcessor for custom processing
@Component
public class CustomBeanPostProcessor implements BeanPostProcessor {
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("BeanPostProcessor.before: " + beanName);
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("BeanPostProcessor.after: " + beanName);
        return bean;
    }
}

// Prototype scope bean
@Component
@Scope("prototype")
public class PrototypeBean {
    // New instance created each time it's requested
    // Spring doesn't manage destruction of prototype beans
}

// Request scope (web applications)
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, 
       proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestScopedBean {
    // New instance per HTTP request
}

// Session scope
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION,
       proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionScopedBean {
    // New instance per HTTP session
}
```

---

## 3. Spring Boot Fundamentals

### Starters

**Understanding Spring Boot Starters:**

```xml
<!-- pom.xml -->
<dependencies>
    <!-- Web starter includes: Spring MVC, REST, Tomcat, Jackson -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Data JPA starter includes: Spring Data JPA, Hibernate -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Security starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- Test starter includes: JUnit, Mockito, AssertJ -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Validation starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Actuator for monitoring -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

```java
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.*;

@SpringBootApplication
// Equivalent to: @Configuration + @EnableAutoConfiguration + @ComponentScan
public class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    // Customize SpringApplication
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }
    
    // Or use SpringApplicationBuilder
    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
            .bannerMode(Banner.Mode.OFF)
            .profiles("dev")
            .run(args);
    }
}

// Exclude specific auto-configuration
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CustomApplication {
}

// Custom starter structure
@Configuration
@ConditionalOnClass(MyLibrary.class)
@EnableConfigurationProperties(MyProperties.class)
public class MyAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public MyService myService(MyProperties properties) {
        return new MyService(properties);
    }
}

@ConfigurationProperties(prefix = "my.service")
public class MyProperties {
    private String endpoint;
    private int timeout = 30;
    private boolean enabled = true;
    
    // Getters and setters
}
```

### Auto-Configuration

**How Auto-Configuration Works:**

```java
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;

// Custom auto-configuration
@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true")
public class CacheAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "com.redis.RedisClient")
    public CacheManager redisCacheManager() {
        return new RedisCacheManager();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public CacheManager defaultCacheManager() {
        return new SimpleCacheManager();
    }
}

// Conditional annotations
@Configuration
public class ConditionalConfig {
    
    // Only if class is present on classpath
    @Bean
    @ConditionalOnClass(name = "com.example.SpecialService")
    public SpecialConfig specialConfig() {
        return new SpecialConfig();
    }
    
    // Only if bean doesn't exist
    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder().build();
    }
    
    // Only if property is set
    @Bean
    @ConditionalOnProperty(
        prefix = "feature",
        name = "advanced",
        havingValue = "true",
        matchIfMissing = false
    )
    public AdvancedFeature advancedFeature() {
        return new AdvancedFeature();
    }
    
    // Only in specific profile
    @Bean
    @Profile("production")
    public ProductionService productionService() {
        return new ProductionService();
    }
    
    // Only if resource exists
    @Bean
    @ConditionalOnResource(resources = "classpath:special-config.xml")
    public SpecialConfig fromXml() {
        return loadFromXml();
    }
}

// Custom condition
public class OnCustomCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, 
                          AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        return "custom".equals(env.getProperty("deployment.type"));
    }
}

@Configuration
@Conditional(OnCustomCondition.class)
public class CustomConditionalConfig {
    // Configuration specific to custom deployment
}

// Debug auto-configuration
// Run with: --debug or application.properties: debug=true
// Or programmatically:
@SpringBootApplication
public class DebugApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DebugApplication.class);
        app.setLogStartupInfo(true);
        app.run(args);
    }
}
```

### Profiles

**Managing Different Environments:**

```yaml
# application.yml
spring:
  application:
    name: my-application
  profiles:
    active: dev

---
# application-dev.yml
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:postgresql://localhost:5432/devdb
    username: devuser
    password: devpass
  logging:
    level:
      root: DEBUG

---
# application-prod.yml
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:postgresql://prod-server:5432/proddb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  logging:
    level:
      root: WARN
```

```java
import org.springframework.context.annotation.*;
import org.springframework.core.env.*;

@Configuration
public class ProfileConfiguration {
    
    @Bean
    @Profile("dev")
    public DataSource devDataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .build();
    }
    
    @Bean
    @Profile("prod")
    public DataSource prodDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(System.getenv("DB_URL"));
        dataSource.setMaximumPoolSize(50);
        dataSource.setMinimumIdle(10);
        return dataSource;
    }
    
    @Bean
    @Profile("test")
    public DataSource testDataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("test-schema.sql")
            .build();
    }
    
    // Multiple profiles
    @Bean
    @Profile({"dev", "staging"})
    public DebugService debugService() {
        return new DebugService();
    }
    
    // NOT operator
    @Bean
    @Profile("!prod")
    public MockExternalService mockService() {
        return new MockExternalService();
    }
    
    // Complex expressions
    @Bean
    @Profile("prod & cloud")
    public CloudProductionService cloudProdService() {
        return new CloudProductionService();
    }
}

// Using Environment
@Component
public class EnvironmentAwareService {
    
    private final Environment environment;
    
    @Autowired
    public EnvironmentAwareService(Environment environment) {
        this.environment = environment;
    }
    
    public void checkEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();
        String[] defaultProfiles = environment.getDefaultProfiles();
        
        boolean isDevProfile = environment.acceptsProfiles(
            Profiles.of("dev")
        );
        
        String property = environment.getProperty("custom.property", 
                                                  "default-value");
    }
}

// Programmatic profile activation
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setAdditionalProfiles("custom-profile");
        app.run(args);
    }
}
```

### Properties and Configuration

**Externalizing Configuration:**

```properties
# application.properties

# Server configuration
server.port=8080
server.servlet.context-path=/api
server.compression.enabled=true

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=user
spring.datasource.password=password
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5

# JPA
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Logging
logging.level.root=INFO
logging.level.com.example=DEBUG
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

# Custom properties
app.name=My Application
app.version=1.0.0
app.features.advanced=true
app.cache.ttl=3600
app.api.timeout=30000
```

```java
import org.springframework.boot.context.properties.*;
import org.springframework.context.annotation.*;
import org.springframework.validation.annotation.*;
import javax.validation.constraints.*;

// Type-safe configuration properties
@ConfigurationProperties(prefix = "app")
@Validated
public class ApplicationProperties {
    
    @NotBlank
    private String name;
    
    @NotBlank
    private String version;
    
    private Features features = new Features();
    private Cache cache = new Cache();
    private Api api = new Api();
    
    public static class Features {
        private boolean advanced;
        private boolean beta;
        
        // Getters and setters
    }
    
    public static class Cache {
        @Min(60)
        @Max(86400)
        private int ttl = 3600;
        
        @NotBlank
        private String provider = "redis";
        
        // Getters and setters
    }
    
    public static class Api {
        @NotBlank
        private String baseUrl;
        
        @Min(1000)
        private int timeout = 30000;
        
        @NotNull
        private List<String> allowedOrigins = new ArrayList<>();
        
        // Getters and setters
    }
    
    // Getters and setters for all fields
}

// Enable configuration properties
@Configuration
@EnableConfigurationProperties(ApplicationProperties.class)
public class AppConfig {
}

// Using configuration properties
@Service
public class ApplicationService {
    
    private final ApplicationProperties properties;
    
    @Autowired
    public ApplicationService(ApplicationProperties properties) {
        this.properties = properties;
    }
    
    public void logConfiguration() {
        System.out.println("Application: " + properties.getName());
        System.out.println("Version: " + properties.getVersion());
        System.out.println("Advanced features: " + 
                         properties.getFeatures().isAdvanced());
    }
}

// Using @Value annotation
@Component
public class ValueBasedConfiguration {
    
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.cache.ttl:3600}")
    private int cacheTtl;
    
    @Value("${app.features.advanced:false}")
    private boolean advancedFeatures;
    
    // SpEL expressions
    @Value("#{${app.cache.ttl} * 1000}")
    private long cacheTtlMillis;
    
    @Value("#{'${app.api.allowed-origins}'.split(',')}")
    private List<String> allowedOrigins;
}

// Profile-specific properties
@Component
@ConfigurationProperties(prefix = "database")
public class DatabaseProperties {
    
    private String url;
    private String username;
    private String password;
    private Pool pool = new Pool();
    
    public static class Pool {
        private int maxSize = 10;
        private int minIdle = 5;
        private long connectionTimeout = 30000;
        
        // Getters and setters
    }
    
    // Getters and setters
}

// Property validation
@ConfigurationProperties(prefix = "security")
@Validated
public class SecurityProperties {
    
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9]{32}$")
    private String secretKey;
    
    @Min(300)
    @Max(86400)
    private int tokenExpiration = 3600;
    
    @NotEmpty
    private List<@NotBlank String> allowedHosts;
    
    // Getters and setters
}

// Configuration processor for IDE autocompletion
// Add to pom.xml:
/*
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
*/
```

---

## 4. Data Layer

### Spring Data JPA

**Repository Pattern and Query Methods:**

```java
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.data.domain.*;
import javax.persistence.*;
import java.util.*;

// Entity
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_username", columnList = "username")
})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, 
               orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and setters, equals, hashCode
}

// Repository with query methods
public interface UserRepository extends JpaRepository<User, Long>, 
                                       JpaSpecificationExecutor<User> {
    
    // Derived query methods
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByStatus(UserStatus status);
    List<User> findByFirstNameAndLastName(String firstName, String lastName);
    List<User> findByFirstNameOrLastName(String firstName, String lastName);
    
    // With ordering
    List<User> findByStatusOrderByCreatedAtDesc(UserStatus status);
    
    // With pagination
    Page<User> findByStatus(UserStatus status, Pageable pageable);
    
    // Top/First
    List<User> findTop10ByOrderByCreatedAtDesc();
    Optional<User> findFirstByOrderByCreatedAtDesc();
    
    // Count and exists
    long countByStatus(UserStatus status);
    boolean existsByEmail(String email);
    
    // Delete methods
    void deleteByStatus(UserStatus status);
    long deleteByCreatedAtBefore(LocalDateTime date);
    
    // Custom JPQL queries
    @Query("SELECT u FROM User u WHERE u.email LIKE %:domain")
    List<User> findByEmailDomain(@Param("domain") String domain);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    // Native SQL queries
    @Query(value = "SELECT * FROM users WHERE status = ?1 " +
                   "AND created_at > ?2", nativeQuery = true)
    List<User> findActiveUsersNative(String status, LocalDateTime since);
    
    // Modifying queries
    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :id")
    int updateUserStatus(@Param("id") Long id, @Param("status") UserStatus status);
    
    // Projections
    @Query("SELECT u.username as username, u.email as email FROM User u")
    List<UserSummary> findAllProjectedBy();
    
    // DTO projection
    @Query("SELECT new com.example.dto.UserDTO(u.id, u.username, u.email) " +
           "FROM User u WHERE u.status = :status")
    List<UserDTO> findUserDTOsByStatus(@Param("status") UserStatus status);
}

// Projection interface
public interface UserSummary {
    String getUsername();
    String getEmail();
    @Value("#{target.firstName + ' ' + target.lastName}")
    String getFullName();
}

// Specification for dynamic queries
public class UserSpecifications {
    
    public static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) -> 
            cb.equal(root.get("status"), status);
    }
    
    public static Specification<User> emailContains(String email) {
        return (root, query, cb) -> 
            cb.like(cb.lower(root.get("email")), 
                   "%" + email.toLowerCase() + "%");
    }
    
    public static Specification<User> createdAfter(LocalDateTime date) {
        return (root, query, cb) -> 
            cb.greaterThan(root.get("createdAt"), date);
    }
    
    public static Specification<User> hasRole(String roleName) {
        return (root, query, cb) -> {
            Join<User, Role> roles = root.join("roles");
            return cb.equal(roles.get("name"), roleName);
        };
    }
}

// Using specifications
@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public Page<User> searchUsers(String email, UserStatus status, 
                                  LocalDateTime since, Pageable pageable) {
        Specification<User> spec = Specification.where(null);
        
        if (email != null) {
            spec = spec.and(UserSpecifications.emailContains(email));
        }
        if (status != null) {
            spec = spec.and(UserSpecifications.hasStatus(status));
        }
        if (since != null) {
            spec = spec.and(UserSpecifications.createdAfter(since));
        }
        
        return userRepository.findAll(spec, pageable);
    }
}
```

### Hibernate

**Advanced Hibernate Concepts:**

```java
import javax.persistence.*;
import org.hibernate.annotations.*;
import java.util.*;

// Entity with caching
@Entity
@Cacheable
@org.hibernate.annotations.Cache(
    usage = CacheConcurrencyStrategy.READ_WRITE
)
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
                   generator = "product_seq")
    @SequenceGenerator(name = "product_seq", 
                      sequenceName = "product_sequence",
                      allocationSize = 50)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal price;
    
    @Lob
    private String description;
    
    // Lazy loading
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY,
               cascade = CascadeType.ALL)
    @BatchSize(size = 10)
    private List<ProductReview> reviews = new ArrayList<>();
    
    // Optimistic locking
    @Version
    private Long version;
    
    // Soft delete
    @Where(clause = "deleted = false")
    @SQLDelete(sql = "UPDATE product SET deleted = true WHERE id = ?")
    private boolean deleted = false;
    
    // Audit fields
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

// Composite primary key
@Embeddable
public class OrderItemId implements Serializable {
    private Long orderId;
    private Long productId;
    
    // equals and hashCode
}

@Entity
public class OrderItem {
    
    @EmbeddedId
    private OrderItemId id;
    
    @ManyToOne
    @MapsId("orderId")
    private Order order;
    
    @ManyToOne
    @MapsId("productId")
    private Product product;
    
    private Integer quantity;
    private BigDecimal price;
}

// Inheritance strategies

// 1. Single Table (default)
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "payment_type")
public abstract class Payment {
    @Id
    @GeneratedValue
    private Long id;
    private BigDecimal amount;
}

@Entity
@DiscriminatorValue("CREDIT_CARD")
public class CreditCardPayment extends Payment {
    private String cardNumber;
    private String cvv;
}

@Entity
@DiscriminatorValue("PAYPAL")
public class PayPalPayment extends Payment {
    private String email;
}

// 2. Joined Table
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Vehicle {
    @Id
    @GeneratedValue
    private Long id;
    private String manufacturer;
}

@Entity
@PrimaryKeyJoinColumn(name = "vehicle_id")
public class Car extends Vehicle {
    private int numberOfDoors;
}

// 3. Table Per Class
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Document {
    @Id
    @GeneratedValue
    private Long id;
    private String title;
}

// Entity graphs for fetch optimization
@Entity
@NamedEntityGraph(
    name = "Order.withItems",
    attributeNodes = {
        @NamedAttributeNode("items"),
        @NamedAttributeNode("customer")
    }
)
@NamedEntityGraph(
    name = "Order.detailed",
    attributeNodes = {
        @NamedAttributeNode(value = "items", 
            subgraph = "items-subgraph"),
        @NamedAttributeNode("customer")
    },
    subgraphs = {
        @NamedSubgraph(
            name = "items-subgraph",
            attributeNodes = {
                @NamedAttributeNode("product")
            }
        )
    }
)
public class Order {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;
    
    @OneToMany(mappedBy = "order")
    private List<OrderItem> items = new ArrayList<>();
}

// Using entity graphs
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    @EntityGraph(value = "Order.withItems", type = EntityGraphType.LOAD)
    Optional<Order> findWithItemsById(Long id);
    
    @EntityGraph(attributePaths = {"items", "customer"})
    List<Order> findAllComplete();
}

// N+1 problem solution
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Join fetch to avoid N+1
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.reviews " +
           "WHERE p.id = :id")
    Optional<Product> findByIdWithReviews(@Param("id") Long id);
    
    // Pagination with join fetch
    @Query("SELECT p FROM Product p")
    Page<Product> findAllProducts(Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.reviews " +
           "WHERE p.id IN :ids")
    List<Product> findByIdsWithReviews(@Param("ids") List<Long> ids);
}
```

### Transactions

**Transaction Management:**

```java
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.*;
import org.springframework.stereotype.*;

@Service
public class TransactionDemoService {
    
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    
    // Basic transactional method
    @Transactional
    public Order createOrder(OrderRequest request) {
        Order order = new Order(request);
        orderRepository.save(order);
        paymentService.processPayment(order);
        return order;
    }
    
    // Read-only transaction (optimization)
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    // Custom timeout
    @Transactional(timeout = 30)
    public void longRunningOperation() {
        // Operation that should complete within 30 seconds
    }
    
    // Rollback for specific exceptions
    @Transactional(rollbackFor = {BusinessException.class})
    public void processWithRollback() throws BusinessException {
        // Will rollback on BusinessException
    }
    
    // No rollback for specific exceptions
    @Transactional(noRollbackFor = {ValidationException.class})
    public void processWithoutRollback() {
        // Will not rollback on ValidationException
    }
    
    // Propagation types
    @Transactional(propagation = Propagation.REQUIRED) // Default
    public void requiredPropagation() {
        // Uses existing transaction or creates new one
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNewPropagation() {
        // Always creates new transaction, suspends existing
    }
    
    @Transactional(propagation = Propagation.NESTED)
    public void nestedPropagation() {
        // Creates nested transaction (savepoint)
    }
    
    @Transactional(propagation = Propagation.MANDATORY)
    public void mandatoryPropagation() {
        // Must be called within existing transaction
    }
    
    @Transactional(propagation = Propagation.NEVER)
    public void neverPropagation() {
        // Must not be called within transaction
    }
    
    // Isolation levels
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void readCommittedIsolation() {
        // Prevents dirty reads
    }
    
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void repeatableReadIsolation() {
        // Prevents dirty and non-repeatable reads
    }
    
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void serializableIsolation() {
        // Highest isolation, prevents phantom reads
    }
}

// Programmatic transaction management
@Service
public class ProgrammaticTransactionService {
    
    private final TransactionTemplate transactionTemplate;
    private final PlatformTransactionManager transactionManager;
    
    @Autowired
    public ProgrammaticTransactionService(
            PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }
    
    public void executeWithTemplate() {
        transactionTemplate.execute(status -> {
            try {
                // Transactional code
                return "Success";
            } catch (Exception e) {
                status.setRollbackOnly();
                return "Failure";
            }
        });
    }
    
    public void executeManually() {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        
        try {
            // Transactional code
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw e;
        }
    }
}

// Distributed transactions
@Service
public class DistributedTransactionService {
    
    @Transactional
    public void distributedOperation() {
        // Multiple resource managers
        // Uses JTA for coordination
    }
}

// Event-driven approach for eventual consistency
@Service
public class EventDrivenService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public Order createOrder(OrderRequest request) {
        Order order = new Order(request);
        orderRepository.save(order);
        
        // Publish event after transaction commits
        eventPublisher.publishEvent(new OrderCreatedEvent(order));
        
        return order;
    }
}

@Component
public class OrderEventListener {
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        // This runs only after the transaction commits
        sendNotification(event.getOrder());
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleOrderRollback(OrderCreatedEvent event) {
        // This runs only if the transaction rolls back
    }
}

// Saga pattern for distributed transactions
@Service
public class OrderSagaOrchestrator {
    
    public void executeOrderSaga(OrderRequest request) {
        Order order = null;
        try {
            // Step 1: Create order
            order = orderService.createOrder(request);
            
            // Step 2: Reserve inventory
            inventoryService.reserveItems(order);
            
            // Step 3: Process payment
            paymentService.processPayment(order);
            
            // Step 4: Confirm order
            orderService.confirmOrder(order);
            
        } catch (Exception e) {
            // Compensating transactions
            if (order != null) {
                paymentService.refund(order);
                inventoryService.releaseItems(order);
                orderService.cancelOrder(order);
            }
            throw new SagaException("Order saga failed", e);
        }
    }
}
```

---

## 5. Security

### Spring Security

**Authentication and Authorization:**

```java
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.method.configuration.*;
import org.springframework.security.config.http.*;
import org.springframework.security.crypto.password.*;
import org.springframework.security.web.*;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true,
    jsr250Enabled = true
)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
            .authorizeRequests()
                .antMatchers("/api/public/**").permitAll()
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .antMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                .antMatchers(HttpMethod.POST, "/api/**").authenticated()
                .anyRequest().authenticated()
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/api/auth/login")
                .defaultSuccessUrl("/dashboard")
                .failureUrl("/login?error=true")
                .and()
            .logout()
                .logoutUrl("/api/auth/logout")
                .logoutSuccessUrl("/login")
                .deleteCookies("JSESSIONID")
                .and()
            .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler());
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

// Custom UserDetailsService
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) 
            throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> 
                new UsernameNotFoundException("User not found: " + username));
        
        return org.springframework.security.core.userdetails.User
            .builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities(getAuthorities(user))
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(!user.isActive())
            .build();
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
            .collect(Collectors.toList());
    }
}

// Method-level security
@Service
public class SecuredService {
    
    @PreAuthorize("hasRole('ADMIN')")
    public void adminOnlyMethod() {
        // Only admins can call this
    }
    
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void userOrAdminMethod() {
        // Users or admins can call this
    }
    
    @PreAuthorize("#username == authentication.principal.username")
    public void ownProfileOnly(String username) {
        // Only the user themselves can call this
    }
    
    @PreAuthorize("@securityService.canAccessOrder(#orderId, authentication)")
    public Order getOrder(Long orderId) {
        // Custom security expression
    }
    
    @PostAuthorize("returnObject.owner == authentication.principal.username")
    public Order getOrderWithPostCheck(Long orderId) {
        // Check after method execution
    }
    
    @PreFilter("filterObject.owner == authentication.principal.username")
    public void processOrders(List<Order> orders) {
        // Filter input collection
    }
    
    @PostFilter("filterObject.public == true or " +
                "filterObject.owner == authentication.principal.username")
    public List<Order> getAllOrders() {
        // Filter return collection
    }
}

// Custom security expression
@Component("securityService")
public class SecurityService {
    
    public boolean canAccessOrder(Long orderId, Authentication authentication) {
        // Custom logic to check if user can access order
        String username = authentication.getName();
        // Check in database
        return true;
    }
    
    public boolean hasPermission(Authentication authentication, 
                                 Object targetDomainObject, 
                                 String permission) {
        // Custom permission logic
        return true;
    }
}
```

### JWT (JSON Web Tokens)

**JWT-based Authentication:**

```java
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.http.*;
import java.security.Key;
import java.util.*;

// JWT Utility
@Component
public class JwtTokenProvider {
    
    private final Key secretKey;
    private final long validityInMilliseconds;
    
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration:3600000}") long validityInMilliseconds) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.validityInMilliseconds = validityInMilliseconds;
    }
    
    public String createToken(String username, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);
        
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }
    
    public String createRefreshToken(String username) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + (validityInMilliseconds * 24));
        
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }
    
    public Authentication getAuthentication(String token) {
        String username = getUsername(token);
        List<String> roles = getRoles(token);
        
        List<GrantedAuthority> authorities = roles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        
        return new UsernamePasswordAuthenticationToken(
            username, "", authorities);
    }
    
    public String getUsername(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        return (List<String>) Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .get("roles");
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

// JWT Authentication Filter
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        String token = resolveToken(request);
        
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

// Security configuration with JWT
@Configuration
@EnableWebSecurity
public class JwtSecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    public JwtSecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated()
                .and()
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                           UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}

// Authentication Controller
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );
            
            User user = userService.findByUsername(request.getUsername());
            List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
            
            String accessToken = jwtTokenProvider.createToken(
                user.getUsername(), roles);
            String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getUsername());
            
            return ResponseEntity.ok(new AuthResponse(
                accessToken, refreshToken, user.getUsername(), roles));
                
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Invalid credentials");
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestBody RefreshTokenRequest request) {
        
        String refreshToken = request.getRefreshToken();
        
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        
        String username = jwtTokenProvider.getUsername(refreshToken);
        User user = userService.findByUsername(username);
        List<String> roles = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toList());
        
        String newAccessToken = jwtTokenProvider.createToken(username, roles);
        
        return ResponseEntity.ok(new AuthResponse(
            newAccessToken, refreshToken, username, roles));
    }
}
```

### OAuth2

**OAuth2 Integration:**

```java
import org.springframework.security.oauth2.config.annotation.web.configuration.*;
import org.springframework.security.oauth2.config.annotation.web.configurers.*;
import org.springframework.security.oauth2.provider.token.*;

// OAuth2 Authorization Server
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) 
            throws Exception {
        clients.inMemory()
            .withClient("web-client")
                .secret(passwordEncoder().encode("web-secret"))
                .authorizedGrantTypes(
                    "password", 
                    "authorization_code", 
                    "refresh_token"
                )
                .scopes("read", "write")
                .accessTokenValiditySeconds(3600)
                .refreshTokenValiditySeconds(86400)
            .and()
            .withClient("mobile-client")
                .secret(passwordEncoder().encode("mobile-secret"))
                .authorizedGrantTypes("password", "refresh_token")
                .scopes("read", "write")
                .accessTokenValiditySeconds(7200);
    }
    
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
            .authenticationManager(authenticationManager)
            .userDetailsService(userDetailsService)
            .tokenStore(tokenStore())
            .accessTokenConverter(accessTokenConverter());
    }
    
    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }
    
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("secret-key");
        return converter;
    }
}

// OAuth2 Resource Server
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/api/public/**").permitAll()
                .antMatchers("/api/**").authenticated();
    }
    
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.tokenStore(tokenStore());
    }
}

// OAuth2 Client Configuration
@Configuration
public class OAuth2ClientConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/", "/login**").permitAll()
                .anyRequest().authenticated()
                .and()
            .oauth2Login()
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .userInfoEndpoint()
                    .userService(oauth2UserService());
        
        return http.build();
    }
    
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        return new CustomOAuth2UserService();
    }
}

// Custom OAuth2 User Service
public class CustomOAuth2UserService 
        extends DefaultOAuth2UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest
            .getClientRegistration()
            .getRegistrationId();
        
        String email = oauth2User.getAttribute("email");
        
        // Find or create user
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> createUser(oauth2User, registrationId));
        
        return new CustomOAuth2User(oauth2User, user);
    }
    
    private User createUser(OAuth2User oauth2User, String provider) {
        User user = new User();
        user.setEmail(oauth2User.getAttribute("email"));
        user.setUsername(oauth2User.getAttribute("name"));
        user.setProvider(provider);
        return userRepository.save(user);
    }
}

// application.yml for OAuth2 clients
/*
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - email
              - profile
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope:
              - user:email
              - read:user
*/
```

This covers the first 5 sections. Would you like me to continue with sections 6-9 (Advanced Features, Microservices & Spring Cloud, Messaging, and DevOps & Deployment)?