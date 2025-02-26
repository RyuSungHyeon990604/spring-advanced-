# 정의한 문제와 해결 과정

---


## **1. 문제 인식 및 정의**

### **문제점 분석**

기존 코드에서 **JWT 파싱을 통한 사용자 인증 처리 방식**에 문제가 있었음.
- `ManagerController`가 인증 처리를 직접 수행하고 있음.
-  `@Auth`를 사용하는 다른 메서드와 인증 방식이 다르게 적용됨.

---

## **2. 해결 방안**

### **2-1. 의사결정 과정**

#### ✅ **인증 방식을 통일**

- 기존 `saveManager()`에서는 `@Auth AuthUser`를 사용하여 인증 정보를 받아오고 있음.
- 반면, `deleteManager()`에서는 `Authorization` 헤더에서 JWT를 직접 파싱하는 방식이었음.
- `deleteManager()`도 `@Auth AuthUser`를 사용하여 인증을 일관되게 적용해야함.
### **2-2. 해결 과정**

#### 🔹 **변경 전 코드 (`deleteManager()`)**

```java
@DeleteMapping("/todos/{todoId}/managers/{managerId}")  
public void deleteManager(  
        @RequestHeader("Authorization") String bearerToken,  
        @PathVariable long todoId,  
        @PathVariable long managerId  
) {  
    Claims claims = jwtUtil.extractClaims(bearerToken.substring(7));  
    long userId = Long.parseLong(claims.getSubject());  
    managerService.deleteManager(userId, todoId, managerId);  
}
```
#### 🔹 **변경 후 코드 (`deleteManager()`)**
```java
@DeleteMapping("/todos/{todoId}/managers/{managerId}")  
public void deleteManager(  
        @Auth AuthUser authUser,  
        @PathVariable long todoId,  
        @PathVariable long managerId  
) {  
    managerService.deleteManager(authUser.getId(), todoId, managerId);  
}
```



## **1. 문제 인식 및 정의**

#### **문제점 분석**

기존 코드에서 **클라이언트 입력 값 검증 부족**으로 인해 발생할 수 있는 문제가 있었음.

- `page=0` 또는 `page=-1` 등의 잘못된 값이 들어와도 서버에서 처리 가능했음.
- **비정상적인 size 값 가능**: `size=0` 또는 `size=-5` 같은 입력도 허용됨.
- 클라이언트 요청 데이터에 대한 명확한 기준 없음

---

## **2. 해결 방안**

### **2-1. 의사결정 과정**

#### ✅ **입력 검증 방식**

- **1. 서버가 자동으로 보정 (ex: Math.max() 활용)**

    - 클라이언트가 잘못된 값을 입력해도, 서버에서 적절한 값으로 변환하여 처리.
    - 예: `page=-1` → `page=1`, `size=0` → `size=10`
    - 하지만 이 방식은 잘못된 데이터가 허용될 수 있으며, 클라이언트가 의도한 요청과 다르게 처리될 가능성이 있음.
- **2. 클라이언트가 올바른 값을 입력하도록 강제 (`@Validated` 활용)**

    - `@Positive` 어노테이션을 사용하여 **0 이하의 값이 들어올 경우 자동으로 예외 발생**.
    - 클라이언트가 올바른 값을 입력해야만 요청이 정상 처리됨.


#### ✅ **결정: 클라이언트가 올바르게 입력하도록 강제 (`@Validated` 활용)**

- 멘토링 결과 클라이언트가 올바른 값을 입력하도록 유도하는 것이 **데이터 무결성 유지 및 예측 가능한 시스템**을 만드는 데 유리함.
- 따라서 `@Positive` 어노테이션을 추가하여 **잘못된 입력을 사전에 차단**하는 방식으로 결정.

### **2-2. 해결 과정**

#### 🔹 **변경 전 코드**

```java
@GetMapping("/todos")  
public ResponseEntity<Page<TodoResponse>> getTodos(  
        @RequestParam(defaultValue = "1") int page,  
        @RequestParam(defaultValue = "10") int size  
) {  
    return ResponseEntity.ok(todoService.getTodos(page, size));  
}
```

---

#### 🔹 **변경 후 코드**

```java
@GetMapping("/todos")  
public ResponseEntity<Page<TodoResponse>> getTodos(  
        @RequestParam(defaultValue = "1") @Positive  int page,  
        @RequestParam(defaultValue = "10") @Positive int size  
) {  
    return ResponseEntity.ok(todoService.getTodos(page, size));  
}
```

##### **변경 사항**

1. `@Positive` 어노테이션 추가 → `page`와 `size`가 **양수(1 이상) 값만 허용**됨.






## **1. 문제 인식 및 정의**

#### **문제점 분석**

기존 코드에서 **불필요한 데이터 변경이 발생**할 가능성이 있었음.

- **이미 같은 역할(UserRole)을 가진 사용자의 정보를 업데이트할 필요가 없음.**

---

## **2. 해결 방안**

### **2-1. 의사결정 과정**

#### ✅ **불필요한 업데이트 방지**

- `user.updateRole()` 호출 전에 **현재 역할과 변경 요청 역할을 비교**하여, 동일할 경우 업데이트 수행을 방지.
- 동일한 값으로 업데이트하려는 경우, **예외를 발생시켜 클라이언트에게 알려줌**.

### **2-2. 해결 과정**

#### 🔹 **변경 전 코드**

```java
@Transactional  
public void changeUserRole(long userId, UserRoleChangeRequest userRoleChangeRequest) {  
    User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));  
    user.updateRole(UserRole.of(userRoleChangeRequest.getRole()));  
}
```

---

#### 🔹 **변경 후 코드**

```java
@Transactional  
public void changeUserRole(long userId, UserRoleChangeRequest userRoleChangeRequest) {  
    User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));  
    //기존과 동일한 UserRole 일경우 업데이트를 수행하지않는다.  
    if(user.getUserRole() == UserRole.of(userRoleChangeRequest.getRole())) {  
        throw new InvalidRequestException("기존과 동일한 권한으로 변경할수없습니다.");  
    }    user.updateRole(UserRole.of(userRoleChangeRequest.getRole()));  
}
```

---

## **1. 문제 인식 및 정의**

#### **문제점 분석**

기존 코드에서 **담당자 중복 등록을 방지하는 로직이 없음.**

- 동일한 `managerUser`가 같은 `todoId`에 **여러 번 등록될 수 있는 문제**가 있음.

---

## **2. 해결 방안**

### **2-1. 의사결정 과정**

#### ✅ **담당자 중복 등록 방지 로직 추가**

- `managerRepository.existsByUserIdAndTodoId(managerUser.getId(), todoId)`을 사용하여 **해당 담당자가 이미 등록된 상태인지 확인**.

### **2-2. 해결 과정**

#### 🔹 검증로직 추가

```java
//담당자 중복여부도 체크해야 함  
if(managerRepository.existsByUserIdAndTodoId(managerUser.getId(), todoId)) {  
    throw new InvalidRequestException("이미 등록된 담당자 입니다.");  
}
```

---



## **1. 문제 인식 및 정의**

#### 🔎 **문제점 분석**

```java
//ManagerService.saveManager()
if (ObjectUtils.nullSafeEquals(user.getId(), managerUser.getId())) {  
    throw new InvalidRequestException("일정 작성자는 본인을 담당자로 등록할 수 없습니다.");  
}
```

```java
//Todo.class
@OneToMany(mappedBy = "todo", cascade = CascadeType.PERSIST)  
private List<Manager> managers = new ArrayList<>();  
  
public Todo(String title, String contents, String weather, User user) {  
    this.title = title;  
    this.contents = contents;  
    this.weather = weather;  
    this.user = user;  
    this.managers.add(new Manager(user, this));  
}
```

##### **🚨 문제점**

1. 두가지의 비지니스 로직이 서로 충돌
    -  Todo는 일정 담당자로 일정 작성자를 등록하고있음.
    - ManagerService.saveManager() 로직에서는 일정작성자는 담당자가 될 수 없음.

---

## **2. 해결 방안**

### **2-1. 의사결정 과정**

#### ✅ **"일정 작성자는 본인을 담당자로 등록할 수 없다"는 조건 제거**

- 일정 생성 시 일정 작성자를 담당자로 추가하는 것이 시스템이 요구하는 비지니스로직에 더 알맞다고 판단.

#### ✅ **중복 등록 검증은 유지**

- 다만, **이미 담당자로 등록된 사용자는 다시 등록되지 않도록 중복 등록 검증 로직은 유지해야 함**.

### **2-2. 해결 과정**

#### 🔹 **변경 전 코드 (`saveManager()`)**

```java
@Transactional  
public ManagerSaveResponse saveManager(AuthUser authUser, long todoId, ManagerSaveRequest managerSaveRequest) {  
    ...
  
    if (ObjectUtils.nullSafeEquals(user.getId(), managerUser.getId())) {  
        throw new InvalidRequestException("일정 작성자는 본인을 담당자로 등록할 수 없습니다.");  
    }  
    //담당자 중복여부도 체크해야 함  
    if(managerRepository.existsByUserIdAndTodoId(managerUser.getId(), todoId)) {  
        throw new InvalidRequestException("이미 등록된 담당자 입니다.");  
    }  
    Manager newManagerUser = new Manager(managerUser, todo);  
    Manager savedManagerUser = managerRepository.save(newManagerUser);  
  
    return new ManagerSaveResponse(  
            savedManagerUser.getId(),  
            new UserResponse(managerUser.getId(), managerUser.getEmail())  
    );  
}
```

---

#### 🔹 **변경 후 코드 (`saveManager()`)**

```java
@Transactional  
public ManagerSaveResponse saveManager(AuthUser authUser, long todoId, ManagerSaveRequest managerSaveRequest) {  
    ...
    /*
    if (ObjectUtils.nullSafeEquals(user.getId(), managerUser.getId())) {  
        throw new InvalidRequestException("일정 작성자는 본인을 담당자로 등록할 수 없습니다.");  
    }  
    */      
  
    //담당자 중복여부도 체크해야 함  
    if(managerRepository.existsByUserIdAndTodoId(managerUser.getId(), todoId)) {  
        throw new InvalidRequestException("이미 등록된 담당자 입니다.");  
    }  
    Manager newManagerUser = new Manager(managerUser, todo);  
    Manager savedManagerUser = managerRepository.save(newManagerUser);  
  
    return new ManagerSaveResponse(  
            savedManagerUser.getId(),  
            new UserResponse(managerUser.getId(), managerUser.getEmail())  
    );  
}
```

---