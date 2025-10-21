## About This Project

The original project was created by [CongNguyen2605](https://github.com/CongNguyen2605).  
I am using this repository as a **practice environment to implement and experiment with Jenkins CI/CD pipelines**.

All GitLab CI/CD configurations, workflows, and related improvements in this repository were **developed by me** for learning purposes.

> **Note:** Credits for the original source code and project structure belong to the original author.
# warehouse-service

## Dependencies

```
 <dependency>
            <groupId>com.apus</groupId>
            <artifactId>base</artifactId>
            <version>2.8.8.new</version>
 </dependency>
  <dependency>
            <groupId>com.apus</groupId>
            <artifactId>common</artifactId>
            <version>1.1.8</version>
  </dependency>

```

```
  <repositories>
        <repository>
            <id>apus-central</id>
            <url>http://194.233.82.115:8081/repository/apus.product.core-group/</url>
        </repository>
    </repositories>
```

Thêm cấu hình vào local `.m2`

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
      <server>
          <id>apus-central</id>
          <username>viewer</username>
          <password>onlyview</password>
          </server>
    </servers>
</settings>
```

## Project Structure - Cấu trúc Project

- Cấu trúc tổng quan 1 Project

```bash
├───src
│   ├───main
│   │   ├───java
│   │   │   └───com
│   │   │       └───apuscorp
│   │   │           └───backend
│   │   │               └───servicename(warehouse, promotion..)
│   │   │                   ├───app
│   │   │                   │   ├───api
│   │   │                   │   ├───dto
│   │   │                   │   └───rest
│   │   │                   ├───core
│   │   │                   │   ├───domain
│   │   │                   │   │   ├───constant
│   │   │                   │   │   ├───converter
│   │   │                   │   │   ├───entity
│   │   │                   │   │   ├───enums
│   │   │                   │   ├───port
│   │   │                   │   │   ├───mapper
│   │   │                   │   │   ├───proxy
│   │   │                   │   │   │   └───dto
│   │   │                   │   │   └───repository
│   │   │                   │   │       └───dto
│   │   │                   │   ├───service
│   │   │                   └───infras
│   │   │                       ├───cache
│   │   │                       ├───config
│   │   │                       ├───proxy
│   │   │                       └───repository
│   │   └───resources
│   │       ├───db
│   │       │   └───changelog
│   │       └───META-INF.services
```
- Cấu trúc project thiết theo kiến trúc Hexagonal Architecture
- Package `app`: chứa các package `api`, `dto`, `rest`
  + Package `api` : Được hiểu như driving side or primary(port từ phía ngoài gọi vào core business logic)
  + Package `dto` : Khai báo các class DTO định nghĩa data nhận và trả về cho client
  + Package `rest`: Khai báo các Controller nhận request và trả response lại cho client. Giao tiếp với business logic thông qua các primary port
- Package core: Là nơi implement các business logic
  + Package `domain` : Chứa các khai báo `entity`, `constant`, `converter`, `enums`
     + `entity`: định nghĩa tất cả các class mapping tới các bảng trong database. Lưu ý không sử dụng các quan hệ **@ManyToMany**, **@ManyToOne**
     + `constant`: khai báo các biến constant dùng chung trong service
     + `converter`: khai báo các class converter
     + `enums` : khai báo các enum dùng trong service
  + Package `port` : Được hiểu như driven side or secondary(port từ phía trong core business gọi ra)
     + `mapper` : Khai báo các interface mapper từ entity sang dto và ngược lại
     + `proxy` : Khai báo các interface liên quan tới việc lấy data từ các service khác
     + `repository`: Khai báo các interface liên quan tới việc lấy data từ database
  + Package `service`: Chứa các class triển khai code nghiệp vụ, nơi giao tiếp giữa controller với persistence layer(thường là một hệ quản trị cơ sở dữ liệu quan hệ – Relational DBMS).
- Package `infras`: Chứa các logic, cấu hình cần thiết để khởi chạy ứng dụng(caching, repository, feign client...). Giao tiếp với business logic thông qua các primary port được định nghĩa bên trong core business
  + Package `proxy`: Nơi implement các logic call api tới các service khác
  + Package `repository`: Nơi implement logic liên quan tơi database

## Khai báo Entity

- `BaseEntity`: đều phải extends BaseEntity.
```java
@Entity
@Table(name = "stock_location")
@Getter
@Setter
public class StockLocationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
```

## Controller

Tất các class Controller cần phải thêm anotation `@ResponseWrapper`

```java

@RequestMapping("/api/v1/stock-location")
@AllArgsConstructor
@RestController
@ResponseWrapper
public class StockLocationController {

  private final StockLocationApi stockLocationService;

  @Operation(summary = "Lấy danh sách vị trí kho")
  @GetMapping("/tiny/list")
  @HandsomePaging
  public Page<StockLocationTinyResponse> getLocationTinyPage(@RequestParam(required = false) Long warehouseId,
                                                             @RequestParam(required = false) Set<Long> locationIds,
                                                             @ParameterObject Pageable pageable) {
    return stockLocationService.getLocationTinyPage(warehouseId, locationIds, pageable);
  }
}
```

## Log Tracing
 - Adding configuration:
```yaml
management:
  zipkin:
    enabled: true
    tracing:
      endpoint: http://zipkin:9411/api/v2/spans
  tracing:
    sampling:
      probability: 0.31
```
## Exception Handling


## API Documents

[Swagger UI](http://localhost:8082/swagger-ui.html)

## Map Struct

## Codding Convention
 Sử dụng plug in Sonarlint của Ide intellij để kiểm tra.
### Ngoài ra cần tuân theo các rule sau:
#### Đặt tên biến
+ Đặt tên biến có ý nghĩa, dễ hiểu, dễ truy xuất lại
 ```java
     // bad
        ProductRepository repository;
        LocalDate localDate = LocalDate.now();
        
     // good
        ProductRepository productRepository;
        LocalDate currentDate = LocalDate.now();
 ```
 ```java
        
     // bad
      String [] l = {"Stock", "Quality", "Packing"};
      for (int i = 0; i < l.length; i++) {
              String li = l[i];
              doSomeThing();
              // `l` là cái gì vậy?
              dispatch(li);
       }
      
      // good
      String[] locations = {"Stock", "Quality", "Packing"};

      for (String location : locations) {
              doSomeThing();
              dispatch(location);
       }
 ```
+ Sử dụng các tên có thể tìm kiếm được 
```java
    // bad
    // 1 thời gian sau không biết 86400000 là gì?
    setTimeout(blastOff, 86400000);
    
    // good
    // Khai báo chúng như một biến hằng global.
    public static final int MILLISECONDS_IN_A_DAY = 86400000;
    setTimeout(blastOff, MILLISECONDS_IN_A_DAY);
```
+ Không thêm từ không cần thiết vào tên biến: nếu tên của class đã nói lên điều gì đó rồi, thì không lặp lại ở tên biến.
```java
    // bad
    class StockLocationEntity {
      private String locationCode;
      private String locationName;
      private String locationType;
    }

    // good
    class StockLocationEntity {
      private String code;
      private String name;
      private String type;
    }
```
#### Phương thức
 + Số lượng param lý tưởng là 4 tham số để tránh tạo ra độ phức tạp cho function khó maintenance, nếu nhiền hơn cần review lại
 + Số line trong 1 function không được dài quá 70 line
 + Một function chỉ nên giải quyết một vấn đề
```java
      // bad
      public void emailClients(List<Client> clients) {
        for (Client client : clients) {
          Client clientRecord = repository.findOne(client.getId());
          if (clientRecord.isActive()){
            email(client);
          }
        }
      }
  
      // good
      public void emailClients(List<Client> clients) {
        for (Client client : clients) {
          if (isActiveClient(client)) {
            email(client);
          }
        }
      }

      private boolean isActiveClient(Client client) {
        Client clientRecord = repository.findOne(client.getId());
        return clientRecord.isActive();
      }
```
+ Tên function phải thể hiện ý nghĩa của function thực hiện

```java
     // bad
    private void getList(String search, Pageable page) {
      //..
    }
    // Không biết get ra danh sách gì
    getList(search, page);

    // good
    private void getListProduct(String search, Pageable page) {
      //..
    }
    getListProduct(search, page);
```
### Xóa hết các code đang comment hoặc không được sử dụng.
```java
      // Bad
      doStuff();
      // doOtherStuff();
      // doSomeMoreStuff();
      // doSoMuchStuff();
    
      // Good
      doStuff();
```
### Sử dụng switch case thay cho if else.
+ Trường hợp có 5 điều kiện trở lên switch case được đánh giá là tối ưu hơn if else
```java
        // Bad
        if (i == 1) {
        ... ; // Branch 1
        } else if (i == 2) {
        ... ; // Branch 2
        } else if (i == 3) {
        ... ; // Branch 3
        }else if (i == 4) {
        ... ; // Branch 4
        }else if (i == n) {
        ... ; // Branch n
        } else {
        ... ; // Branch n + 1
        }
        
        // Good
        switch (i) {
            case 1 :
            ... // Branch 1
                break;
            case 2 :
            ... // Branch 2
               break;
            case 3 :
            ... // Branch 2
              break;
            case 4 :
            ... // Branch 2
              break;
            case n :
            ... // Branch n
               break;
            default :
            ... // Branch n + 1
              break;  
        }
```
### Sử dụng phương thức isEmpty() để kiểm tra giá trị giỗng thay vì size()
+ isEmpty nhanh hơn size() nhiều lần, độ phức tạp của isEmpty luôn là O(1) còn size() có thể là O(1) hoặc O(n)
```java
   // Bad
    List<User> users = ...;
    if (users.size() == 0) {
            ...
    }
    Map<Long, User> userMap = ...;
    if (userMap.size() == 0) {
            ...
    }
    
    // Good
    List<User> userList = ...;
    if (userList.isEmpty()) {
            ...
    }
    Map<Long, User> userMap = ...;
    if (userMap.isEmpty()) {
            ...
    }

```
### Hash Map
+ Tránh việc tìm kiếm bị lặp lại, thay vì xác định xem dữ liệu có tồn tại không trước khi sử dụng thì nên truy xuất dữ liệu luôn rồi kiểm tra
```java
    // Bad
    public static User transUser(User user, Map<Long, Role> roleMap) {
        User user = new UserVO();
        user.setId(user.getId());
        if (roleMap.contains(user.getRoleId())) {
          Role role = roleMap.get(user.getRoleId());
          user.setRole(transRole(role));
        }
    }
    
    // Good
    public static User transUser(UserDO user, Map<Long, Role> roleMap) {
        User user = new UserVO();
        user.setId(user.getId());
        Role role = roleMap.get(user.getRoleId());
        if (Objects.nonNull(role)) {
          user.setRole(transRole(role));
        }
    }
```
### Không sử dụng nhiều if else lồng nhau
```java
       // Bad
public class DiscountCalculator {
    String customerType = "VIP"; // Loại khách hàng
    int purchaseAmount = 500;    // Số tiền mua hàng

    if (customerType.equals("VIP")) {
      if (purchaseAmount > 100) {
        if (purchaseAmount < 500) {
          // do something
        } else {
          if (purchaseAmount < 1000) {
            // do something
          } else {
            // do something
          }
        }
      } else {
        // do something
      }
    } else {
      if (customerType.equals("Regular")) {
        if (purchaseAmount > 100) {
          if (purchaseAmount < 500) {
            // do something
          } else {
            // do something
          }
        } else {
          // do something
        }
      } else {
        // do something
      }
    }
}
   // Good
   public class DiscountCalculator {
     public static void main(String[] args) {
       String customerType = "VIP"; // Loại khách hàng
       int purchaseAmount = 500;    // Số tiền mua hàng

       int discount = calculateDiscount(customerType, purchaseAmount);
       if (discount > 0) {
         // do something
       } else {
         // do something
       }
     }

     // Phương thức để tính mức giảm giá
     public static int calculateDiscount(String customerType, int purchaseAmount) {
       switch (customerType) {
         case "VIP":
           return calculateVIPDiscount(purchaseAmount);
         case "Regular":
           return calculateRegularDiscount(purchaseAmount);
         default:
           return 0;
       }
     }

     // Phương thức tính giảm giá cho khách hàng VIP
     public static int calculateVIPDiscount(int purchaseAmount) {
     }
     
     // Phương thức tính giảm giá cho khách hàng thường
     public static int calculateRegularDiscount(int purchaseAmount) {
     }
   }
```
### Không hardcode
+ Đối với các giá trị áp dụng cho đa ngôn ngữ cần được lấy ra từ i18n
```java
    // Bad
     switch (orderResponse.getReturnMethod()) {
        case "CASH":
            returnMethod = "Tiền mặt";
                    break;
                    case "BANK_TRANSFER":
            returnMethod = "Chuyển khoản";
                    break;
                    case "CREDIT":
            returnMethod = "Tín dụng";
                    break;
                    case "DEDUCTION_OF_LIABILITIES":
            returnMethod = "Công nợ";
                    break;
            default:
                    break;
        }
    // Good
     private final TranslateService translateService;

     switch (orderResponse.getReturnMethod()) {
            case "CASH":
                 returnMethod = translateService.translate("cash");
                 break;
            case "BANK_TRANSFER":
                  returnMethod = translateService.translate("bankTransfer");
                  break;
            case "CREDIT":
                  returnMethod = translateService.translate("credit");
                  break;
            case "DEDUCTION_OF_LIABILITIES":
                  returnMethod = translateService.translate("deductionOfLiabilities");;
                  break;
            default:
                break;
            }
```
+ Với các giá trị không bao giờ thay đổi thì bắt buộc phải define ra các biến constant hoặc enum
```java
     // Bad
     switch (orderResponse.getReturnMethod()) {
            case "CASH":
                    returnMethod = translateService.translate("cash");
                    break;
            case "BANK_TRANSFER":
                   returnMethod = translateService.translate("bankTransfer");
                    break;
            case "CREDIT":
                    returnMethod = translateService.translate("credit");
                    break;
            case "DEDUCTION_OF_LIABILITIES":
                    returnMethod = translateService.translate("deductionOfLiabilities");;
                    break;
            default:
                    break;
            }
            
     // Good
     public static final String CASH = "CASH";
     public static final String BANK_TRANSFER = "BANK_TRANSFER";
     public static final String CREDIT = "CREDIT";
     public static final String DEDUCTION_OF_LIABILITIES = "DEDUCTION_OF_LIABILITIES";
     
     switch (orderResponse.getReturnMethod()) {
        case Constants.CASH:
            returnMethod = translateService.translate("cash");
            break;
        case Constants.BANK_TRANSFER:
            returnMethod = translateService.translate("bankTransfer");
            break;
        case Constants.CREDIT:
            returnMethod = translateService.translate("credit");
            break;
        case Constants.DEDUCTION_OF_LIABILITIES:
            returnMethod = translateService.translate("deductionOfLiabilities");;
            break;
        default:
            break;
        }
```
## Registry
