<img src="https://pds.joins.com/news/component/joongang_sunday/202102/06/3dd5eeae-ccd8-4a01-841e-1b5435c8bb85.jpg" height="200" />


# 애완동물 돌봄 서비스 (Petmily)

# Table of contents

- [서비스명 - 애완동물 돌봄 서비스](#---)
    - [서비스 시나리오](#1-서비스-시나리오)
    - [체크포인트](#2-체크포인트)
    - [분석/설계](#3-분석설계)
    - [구현](#4-구현)
        - [DDD의 적용](#41-DDD의-적용)
        - [서비스간 동기식 호출](#42-서비스간-동기식-호출)
        - [서비스간 비동기식 호출](#43-서비스간-비동기식-호출)
        - [환경 설정 (Match pom.xml)](#44-환경-설정-Match-pomxml)
    - [배포](#5-배포Container)
        - [CI/CD](#5-배포Container)
    - [운영](#6-운영)
        - [Auto-Scaler Policy](#61-Auto-Scaler-Policy)
        - [블루-그린 배포](#62-블루-그린-배포)
        - [모니터링](#63-모니터링)
        - [로깅](#64-로깅)

# 1. 서비스 시나리오

도그메이트 커버하기 - https://www.dogmate.co.kr/

기능적 요구사항
1. 고객이 회원으로 등록한다.
2. 회원으로 등록한 고객이 로그인을 한다.
3. 회원이 애완동물을 등록한다.
4. 회원이 시터를 지원한다.
5. 관리자가 시터를 평가하여 등록 또는 거절한다.
6. 회원이 애완동물을 선택하여 맡기기 위한 정보를 입력한다.
7. 회원이 시터를 선택하여 매칭 요청을 한다.
8. 시터가 회원이 맡기기 원하는 애완동물을 선택하여 매칭 요청을 한다.
9. 회원과 시터가 각자의 정보를 보며 매칭을 승인하거나 거절한다.
10. 회원과 시터가 서로 매칭되고나면 애완동물의 돌봄이 시작된다.
11. 회원이 돌봄 중인 애완동물의 일지를 조회한다.
12. 시터가 돌보고 있는 애완동물의 일지를 작성한다.
13. 회원이 돌봄이 끝난 시터를 평가한다.
14. 시터가 돌봄이 끝난 애완동물을 평가한다.

비기능적 요구사항
1. 트랜잭션
    1. 매칭 시 회원 정보와 애완동물 정보는 실시간으로 조회해서 보여줘야 한다. Sync 호출
1. 장애격리
    1. 매칭 기능이 수행되지 않더라도 회원 등록 및 애완동물 등록 가능해야 한다.   Async (event-driven), Eventual Consistency
    2. 평가 기능이 수행되지 않더라도 사용자, 매칭, 애완동물, 일지 등록 기능은 정상 작동해야 한다. Async (event-driven), Eventual Consistency
    1. 매칭 기능에 과부하가 걸리면 매칭을 잠시동안 진행하지 않고 잠시후에 하도록 유도한다.  Circuit breaker, fallback
1. 성능
    1. 회원이 돌봄 상태를 실시간으로 조회할 수 있어야 한다.  CQRS


# 2. 체크포인트

- 분석 설계


- 이벤트스토밍:
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?

- 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
        - 적어도 3개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
- 컨텍스트 매핑 / 이벤트 드리븐 아키텍처
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

- 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?

- 구현
    - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
        - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
        - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
        - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
    - Request-Response 방식의 서비스 중심 아키텍처 구현
        - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
        - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
    - 이벤트 드리븐 아키텍처의 구현
        - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
        - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
        - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
        - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
        - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

    - 폴리글랏 플로그래밍
        - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
        - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?
    - API 게이트웨이
        - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
        - 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?
- 운영
    - SLA 준수
        - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
        - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
        - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
        - 모니터링, 앨럿팅:
    - 무정지 운영 CI/CD (10)
        - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명
        - Contract Test :  자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?


# 3. 분석/설계


## AS-IS 조직 (Horizontally-Aligned)
![image](https://user-images.githubusercontent.com/487999/79684144-2a893200-826a-11ea-9a01-79927d3a0107.png)

## TO-BE 조직 (Vertically-Aligned)
![image](https://user-images.githubusercontent.com/487999/79684159-3543c700-826a-11ea-8d5f-a3fc0c4cad87.png)


## 3.1 Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과:  http://msaez.io/#/storming/nZJ2QhwVc4NlVJPbtTkZ8x9jclF2/every/a77281d704710b0c2e6a823b6e6d973a/-M5AV2z--su_i4BfQfeF


### 이벤트 도출
![image](https://user-images.githubusercontent.com/487999/79683604-47bc0180-8266-11ea-9212-7e88c9bf9911.png)

### 부적격 이벤트 탈락
![image](https://user-images.githubusercontent.com/487999/79683612-4b4f8880-8266-11ea-9519-7e084524a462.png)

    - 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
        - 주문시>메뉴카테고리선택됨, 주문시>메뉴검색됨 :  UI 의 이벤트이지, 업무적인 의미의 이벤트가 아니라서 제외

### 액터, 커맨드 부착하여 읽기 좋게
![image](https://user-images.githubusercontent.com/487999/79683614-4ee30f80-8266-11ea-9a50-68cdff2dcc46.png)

### 어그리게잇으로 묶기
![image](https://user-images.githubusercontent.com/487999/79683618-52769680-8266-11ea-9c21-48d6812444ba.png)

    - app의 Order, store 의 주문처리, 결제의 결제이력은 그와 연결된 command 와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 그들 끼리 묶어줌

### 바운디드 컨텍스트로 묶기

![image](https://user-images.githubusercontent.com/487999/79683625-560a1d80-8266-11ea-9790-40d68a36d95d.png)

    - 도메인 서열 분리 
        - Core Domain:  app(front), store : 없어서는 안될 핵심 서비스이며, 연견 Up-time SLA 수준을 99.999% 목표, 배포주기는 app 의 경우 1주일 1회 미만, store 의 경우 1개월 1회 미만
        - Supporting Domain:   marketing, customer : 경쟁력을 내기위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 각 팀의 자율이나 표준 스프린트 주기가 1주일 이므로 1주일 1회 이상을 기준으로 함.
        - General Domain:   pay : 결제서비스로 3rd Party 외부 서비스를 사용하는 것이 경쟁력이 높음 (핑크색으로 이후 전환할 예정)

### 폴리시 부착 (괄호는 수행주체, 폴리시 부착을 둘째단계에서 해놔도 상관 없음. 전체 연계가 초기에 드러남)

![image](https://user-images.githubusercontent.com/487999/79683633-5aced180-8266-11ea-8f42-c769eb88dfb1.png)

### 폴리시의 이동과 컨텍스트 매핑 (점선은 Pub/Sub, 실선은 Req/Resp)

![image](https://user-images.githubusercontent.com/487999/79683641-5f938580-8266-11ea-9fdb-4e80ff6642fe.png)

### 완성된 1차 모형

![image](https://user-images.githubusercontent.com/487999/79683646-63bfa300-8266-11ea-9bc5-c0b650507ac8.png)

    - View Model 추가

### 1차 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

![image](https://user-images.githubusercontent.com/487999/79684167-3ecd2f00-826a-11ea-806a-957362d197e3.png)

    - 고객이 메뉴를 선택하여 주문한다 (ok)
    - 고객이 결제한다 (ok)
    - 주문이 되면 주문 내역이 입점상점주인에게 전달된다 (ok)
    - 상점주인이 확인하여 요리해서 배달 출발한다 (ok)

![image](https://user-images.githubusercontent.com/487999/79684170-47256a00-826a-11ea-9777-e16fafff519a.png)
- 고객이 주문을 취소할 수 있다 (ok)
- 주문이 취소되면 배달이 취소된다 (ok)
- 고객이 주문상태를 중간중간 조회한다 (View-green sticker 의 추가로 ok)
- 주문상태가 바뀔 때 마다 카톡으로 알림을 보낸다 (?)


### 모델 수정

![image](https://user-images.githubusercontent.com/487999/79684176-4e4c7800-826a-11ea-8deb-b7b053e5d7c6.png)

    - 수정된 모델은 모든 요구사항을 커버함.

### 비기능 요구사항에 대한 검증

![image](https://user-images.githubusercontent.com/487999/79684184-5c9a9400-826a-11ea-8d87-2ed1e44f4562.png)

    - 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
        - 고객 주문시 결제처리:  결제가 완료되지 않은 주문은 절대 받지 않는다는 경영자의 오랜 신념(?) 에 따라, ACID 트랜잭션 적용. 주문완료시 결제처리에 대해서는 Request-Response 방식 처리
        - 결제 완료시 점주연결 및 배송처리:  App(front) 에서 Store 마이크로서비스로 주문요청이 전달되는 과정에 있어서 Store 마이크로 서비스가 별도의 배포주기를 가지기 때문에 Eventual Consistency 방식으로 트랜잭션 처리함.
        - 나머지 모든 inter-microservice 트랜잭션: 주문상태, 배달상태 등 모든 이벤트에 대해 카톡을 처리하는 등, 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함.




## 헥사고날 아키텍처 다이어그램 도출

![image](https://user-images.githubusercontent.com/487999/79684772-eba9ab00-826e-11ea-9405-17e2bf39ec76.png)


    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐

# 3. 분석/설계
## 1) Outer/Inner Architecture
<img src="https://postfiles.pstatic.net/MjAyMTA3MTZfMTAw/MDAxNjI2MzY4MTQ0OTc2.N-i7XwIgqObq2tixuDgotFDOR0wdkSwOyq75XwoYcYcg.pgq6TyfzW3W2QDUpHv9TslMbaTVhTRzMDAekbFiM4bAg.JPEG.ttann/architecture.JPG?type=w966">

## 2) 구현 패턴
###  3-1) Database per service 
###### - framework : mairaDB
###### - 각 마이크로 서비스별 분리된 Database를 사용한다 


| 서비스 | DB|
| -------- | -------- | 
| User    | factory-zdb-petdb-mariadb.factory-zdb:3306/petmily-user     |
| Pet    | factory-zdb-petdb-mariadb.factory-zdb:3306/petmily-pet     |
| Match    |factory-zdb-petdb-mariadb.factory-zdb:3306/petmily-match     |
| Diary    | factory-zdb-petdb-mariadb.factory-zdb:3306/petmily-diary     |
| Assess    | factory-zdb-petdb-mariadb.factory-zdb:3306/petmily-assess     |


### 3-2) Service Registry  & API Gateway 
###### - framework : Ingress
######  - 각 마이크로서비스는 독립된 dns url을 가지며, ingress에 설정된 route rule에 의해 트래픽 라우팅 된다. 
######  - 유형 : DNS 기반(kube-dns) 적용 
 
| 서비스 | DB|
| -------- | -------- | 
| User    | http://petmily.factory-dev.cloudzcp.com/user    |
| Pet    | http://petmily.factory-dev.cloudzcp.com/pet     |
| Match    |http://petmily.factory-dev.cloudzcp.com/match    |
| Diary    | http://petmily.factory-dev.cloudzcp.com/diary    |
| Assess    | http://petmily.factory-dev.cloudzcp.com/assess     |

###### - 설정 : Ingress 설정
```
##### petmily-ingress.yaml
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  annotations:
    kubernetes.io/ingress.class: public-nginx
    nginx.ingress.kubernetes.io/rewrite-target: /$1
  name: petmily-ingress
  namespace: petmily
spec:
  rules:
  - host: petmily.factory-dev.cloudzcp.com
    http:
      paths:
      - backend:
          serviceName: petmily-front-app-dev
          servicePort: 8280
        path: /(.*)
      - backend:
          serviceName: petmily-assess-app-dev
          servicePort: 8080
        path: /assess/(.*)
      - backend:
          serviceName: petmily-diary-app-dev
          servicePort: 8180
        path: /diary/(.*)
      - backend:
          serviceName: petmily-match-app-dev
          servicePort: 8380
        path: /match/(.*)
      - backend:
          serviceName: petmily-pet-app-dev
          servicePort: 8480
          path: /pet/(.*)
      - backend:
          serviceName: petmily-user-app-dev
          servicePort: 8580
        path: /user/(.*)
        
```
### 3-3) Client-side UI Composition
######  - framework  : react + MVVN 패턴
######  frontend도 backend 마이크로서비스처럼 기능별로 분리하고 이를 조합하기 위한 frame 형태의 부모창을 통해 각 frontend component를 조합하여 동작하게 한다. 부모서비스는 틀만 가지며 실제 각 기능표현은 frontend component가 구현하게 한다.  비스니스 규현을 위해 frontend는 여러개의 backend 마이크로서비스 API를 호출한다
######  - 적용사례 : Petmily main 화면내 sitter, pet, 리뷰 조회는 별개의 frontend component가 수행하며, main화면은 화면의 틀
<img src="https://postfiles.pstatic.net/MjAyMTA3MTZfMjg3/MDAxNjI2MzY5NDg5ODEy.au99670qK10EDXuPdbVbnm2NdRm_Llxu8vmvhac92ksg.44LyJLgcJJ5WyuuGGmlcB8vDWYZu1W6fqVC0j1Vc06kg.JPEG.ttann/Client-side_UI_Composition.JPG?type=w966">



### 3-4) 인증/인가 패턴
######  - framework  : react Security(JWT(JSON Web Token) 기반) + redis 
######  - 적용사례 : 사용자 로그인시 Token을 발행하고, 리소스 접근시 토큰을 확인하여 인가 허용한다. 사용자의 시터 등록 승인기능은 ADMIN_ROLE을 가진 Admin 계정만 가능하도록 권한 구성하였음.
######  - 관련 로직  :
```
##### Token Provider (user)
@Service
public class TokenProvider {

	private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

	private static final String TOKEN_SECRET = "2948404D6351655468576D5A7134743777217A25432A462D4A614E645267556A586E3272357538782F413F4428472B4B6250655368566D597033733676397924";

	final static public String ACCESS_TOKEN_NAME = "accessToken";
	final static public String REFRESH_TOKEN_NAME = "refreshToken";

	public final static long TOKEN_VALIDATION_SECOND = 1000 * 60 * 60;
	public final static long REFRESH_TOKEN_VALIDATION_SECOND = 1000 * 60 * 60 * 24;

	public String createToken(User user) {
		return doGenerateToken(user.getLoginId(), TOKEN_VALIDATION_SECOND);
	}
.....
```
```
##### Security Config (user)
....
@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.cors()
				.and()
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
				.csrf()
				.disable()
				.formLogin()
				.loginPage("/login")
				.disable()
				.httpBasic()
				.disable()
				.exceptionHandling()
				.authenticationEntryPoint(new RestAuthenticationEntryPoint())
				.and()
				.authorizeRequests()
				.antMatchers("/api/v1/user/login", "/api/v1/user/create")
				.permitAll()
				.antMatchers(
						"/error",
						"/favicon.ico",
						"/**/*.png",
						"/**/*.gif",
						"/**/*.svg",
						"/**/*.jpg",
						"/**/*.html",
						"/**/*.css",
						"/**/*.js")
				.permitAll()
				.antMatchers("/api/v1/user/**").access("hasRole('ROLE_USER')")
				.antMatchers("/api/v1/sitter/update/**").access("hasRole('ROLE_ADMIN')")
				.anyRequest()
				.authenticated();
           
...

```

### 3-5) 기타

###### - Circuit Breaker : Petmily 사이트는 매치상태, 회원/시터상태 변경시 비동기, 이벤트 기반으로 처리하여 별도로 Circuit Breaker 적용하지 않았음. 




# 4. 구현

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8180 ~ 8N80 이다)

```
cd petmily-user
mvn spring-boot:run

cd petmily-pet
mvn spring-boot:run

cd petmily-match
mvn spring-boot:run

cd petmily-diary
mvn spring-boot:run 

cd petmily-assess
mvn spring-boot:run

```

## 4.1 DDD의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (예시는 diary 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다. 언어는 영어로 통일하였다. 

```
package com.petmily.diary.domain.entity;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.Table;

import org.springframework.beans.BeanUtils;

import com.petmily.diary.domain.enums.DiaryStatus;
import com.petmily.diary.domain.vo.DiaryDetail;
import com.petmily.diary.kafka.event.DiaryDeleted;
import com.petmily.diary.kafka.event.DiaryModified;
import com.petmily.diary.kafka.event.DiaryRegistered;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "TB_DIARY")
@Slf4j
public class Diary extends BaseTimeEntity {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Embedded
	private DiaryDetail detail;
	
	private LocalDate registerDate;
	
	@Enumerated(EnumType.STRING)
	private DiaryStatus diaryStatus;
	
	@ManyToOne
	@JoinColumn(name = "DIARY_GROUP_ID")
	private DiaryGroup diaryGroup;
	
	@Builder
	public Diary(LocalDate registerDate, DiaryGroup diaryGroup, DiaryDetail detail) {
		this.registerDate = registerDate;
		this.diaryGroup = diaryGroup;
		this.detail = detail;
		
		setDiaryStatus(DiaryStatus.OPEN);
	}
	
	public void setDiaryStatus(DiaryStatus diaryStatus) {
		this.diaryStatus = diaryStatus;
	}
	
	/*
	 * event
	 */
	@PostPersist
	private void afterPersist() {
		log.info("diary afterPersist : " + this);
		DiaryRegistered diaryRegistered = new DiaryRegistered();
		BeanUtils.copyProperties(this, diaryRegistered);
		diaryRegistered.publishAfterCommit();
	}
	
	@PostUpdate
	private void afterUpdate() {
		log.info("diary afterUpdate : " + this);
		DiaryModified diaryModified = new DiaryModified();
		BeanUtils.copyProperties(this, diaryModified);
		diaryModified.publishAfterCommit();
	}
	
	@PostRemove
	private void afterRemove() {
		log.info("diary afterRemove : " + this);
		DiaryDeleted diaryDeleted = new DiaryDeleted();
		BeanUtils.copyProperties(this, diaryDeleted);
		diaryDeleted.publishAfterCommit();
	}
}

```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB)에 대한 데이터 접근영역을 구현한다.
```
package com.petmily.diary.domain.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.petmily.diary.domain.entity.Diary;
import com.petmily.diary.domain.entity.DiaryGroup;

public interface DiaryRepository extends PagingAndSortingRepository<Diary, Long> {

	List<Diary> findByDiaryGroupOrderByIdDesc(DiaryGroup diaryGroup);
}
```
- REST API 명세 및 테스트(Swagger)
```
# 다이어리 서비스
http://petmily.factory-dev.cloudzcp.com/diary/swagger-ui.html

# 평가 서비스
http://petmily.factory-dev.cloudzcp.com/assess/swagger-ui.html

# 매칭 서비스
http://petmily.factory-dev.cloudzcp.com/match/swagger-ui.html

# 팻 서비스
http://petmily.factory-dev.cloudzcp.com/pet/swagger-ui.html

# 유저 서비스
http://petmily.factory-dev.cloudzcp.com/user/swagger-ui.html

```

## 4.2 서비스간 동기식 호출

분석단계에서의 조건 중 하나로 매칭서비스->유저서비스 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 각 서비스 별로 Open하고 있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다.

- 매칭 서비스에서 유저 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현

```
# (Match) UserFeignSyncService.java

package com.petmily.match.service;

import com.petmily.match.domain.dto.SitterTransferDto;
import com.petmily.match.domain.vo.user.Address;
import com.petmily.match.domain.vo.user.User;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "petmilyUserSync", url = "${external-api.petmily-user.url}")
public interface UserFeignSyncService {

	@GetMapping(value = "/api/v1/user/{loginId}")
	ResponseEntity<User> getUser(@PathVariable("loginId") String username);
	

	@GetMapping(value = "/api/v1/user/sitter/available")
	ResponseEntity<List<SitterTransferDto>> getAvailableSitter(Address address);

}
```

- 시터 이력서 등록
```
# (Match) PetCareApplicationService.java (Service)

	public PetCareApplication registerApplication(PetCareApplication application) {

		...

		ResponseEntity<User> result = userFeignSyncService.getUser(username);
		if (!HttpStatus.OK.equals(result.getStatusCode()) || CommonUtil.isEmpty(result.getBody())) {
			throw new BusinessException("사용자 정보를 찾을 수 없습니다.");
		}

		User user = result.getBody();

		...
	}
```

## 4.3. 서비스간 비동기식 호출 

분석단계에서의 조건 중 하나로 돌봄시작(match)->다이어리작성시작(diary)/ 간의 호출은 비동기식 이벤트기반 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 Entity의 LifeCycle @PostPersist Annotation을 통해, 카프카로 이벤트를 보내고, 다이어리에서 이벤트를 수신하는 방식으로 구현하였다.

- 펫 돌봄(PetCare)에 기록을 남긴 후에 곧바로 돌봄시작 되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
- TransactionSynchronizationManager 통해 시스템 간 Data 일관성이 유지될 수 있도록 처리 

```
# (match) PetCare.java (Entity)

...

    @PostPersist
    public void onPostPersist(){
        CareServiceStarted careServiceStarted = new CareServiceStarted();
        BeanUtils.copyProperties(this, careServiceStarted);
        careServiceStarted.publishAfterCommit();
    }
    
...
	public void publishAfterCommit() {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void afterCompletion(int status) {
				AbstractEvent.this.publish();
			}
		});
	}
...

```

- 펫 돌봄시작 이벤트 수신 정책을 구현한다. 펫 돌봄 이벤트를 받은 후, 다이어리 그룹을 생성한다.
- JsonObject 기반으로 Topic 처리를 의도 하였으나 각 서비스 간의 Package NamingRule 설정상의 문제로 String 기반으로 처리 하였음
```
# (diary) EventListener.java (Entity)

...

    @KafkaListener(topics = "${kafka.topic}", groupId = "${kafka.consumer.groupId}")
	public void listenEvent(String event) {

		try {
			AbstractEvent abstractEvent = deserializeEvent(event);

			logger.info("##### Received Message {}", abstractEvent.getClass().getSimpleName());
			logger.info("##### " + CommonUtil.toJsonString(abstractEvent));

			String eventType = abstractEvent.getEventType();

            switch (eventType) {
                case "CareServiceStarted":
                    logger.info("event CareServiceStarted {}", eventType);
                    CareServiceStarted careServiceStarted = (CareServiceStarted)abstractEvent;
                    diaryGroupService.saveNew(careServiceStarted);
                    break;
                    
                    ...

                default:
                    logger.info("{} event received. skipped", eventType);
                    break;
                }
		} catch (SerializationException e) {
			logger.info("Undefined event received. skipped");
		}

		latch.countDown();
	}

...

```

## 4.4. 환경 설정 (Match pom.xml)

주요 기술 및 라이브러리를 설명한다

* Spring Boot
* Spring Data JPA (ORM)
* Spring FeignClient (서비스간 동기식 호출)
* Spring Cloud Stream Kafka (서비스간 비동기식 호출)
* Spring Data Redis (JWT 기반 서비스간 Session 처리)
* Spring Security (Authentication/Authorization)
* MariaDB (RDB)  
* Spring Fox / Swagger 
```
# Match pom.xml lib dependency 영역
...

        <!-- Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.mariadb.jdbc</groupId>
            <artifactId>mariadb-java-client</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- kafka -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-stream-binder-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka-test</artifactId>
            <version>2.6.7</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>kafka</artifactId>
            <version>1.15.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
            <version>2.9.0</version>
        </dependency>

        <!-- Rest Client -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <!-- Swagger -->
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-boot-starter</artifactId>
            <version>3.0.0</version>
        </dependency>
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger-ui</artifactId>
            <version>2.10.5</version>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.11.1</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.11.1</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.11.1</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.session</groupId>
            <artifactId>spring-session-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>redis.clients</groupId>
            <artifactId>jedis</artifactId>
            <version>3.5.2</version>
            <scope>test</scope>
        </dependency>

    </dependencies>

...

``` 
# 5. 배포/Container

## 5.1 ZCP  PipeLine을 통한 CI/CD 구축 확인

### 5.1.1 CloudZ CP : DevOps > Pipeline

<img src="https://user-images.githubusercontent.com/67447558/125728606-33ee0229-d2cd-4976-8a0c-3606550a5b8a.PNG" width="700" height="370">

### 5.1.2 Jenkins : petmily 선택후 배포를 원하는 Jenkins Project의 cicd 선택
<img src="https://user-images.githubusercontent.com/67447558/125728936-e6eac2a9-d837-4083-b3f6-c940182f78fd.PNG" width="700" height="370">

### 5.1.3 Jenkins 스크립트내 customize 부분
- Git로 부터 소스 빌드 및 Docker image build, Deploy까지 정의 하여 실행함
<img src="https://user-images.githubusercontent.com/67447558/125729858-e572973c-616b-4cf5-ae4a-c0a12b47cfd5.PNG" width="700" height="370">

     그림) petmily-pet-Jenkinsfile-cicd내 config 설정 > pipeline 관련

<img src="https://user-images.githubusercontent.com/67447558/125730203-e07b71ca-1e9b-4b14-a6b9-dddb70386ae0.PNG" width="700" height="370">

     그림) pipeline 관련 실행스크립트 


     - 실행 단계 : SOURCE CHECKOUT / BUILD / BUILD DOCKER IMAGE / BUILD K8S YAML / DEPLOY


## 5.2 Sonar / Test 설정 내역

### 5.2.1 SonarQube Test 관련 설정
- 먼저, SonarQube 관련 설정을 한 후( ​c:\sonar-scanner\conf, C:\sonarqube\conf), sonarqube > sonar-scanner를 기동한다.

- SonarQube Test 관련 설정을 Springboot project의 root 경로에 생성한다.
<img src="https://user-images.githubusercontent.com/67447558/125729119-9c1a24a1-78e7-4461-bd55-68bee0fb1523.PNG"  width="700" height="370">

### 5.2.2 SonarQube 결과 대쉬보드
- Petmily내 MSA서비스들에 대하여 SonarQube에서 제공하는 다양한 품질 항목에 대해서 확인할 수 있다
<img src="https://user-images.githubusercontent.com/67447558/125293787-b64f1880-e35e-11eb-95bb-edeec0d50f99.PNG"  width="700" height="370">

   * 주요 기능 : 코드 품질 대쉬보드, 코드 중복, 코딩표준, 커버리지 부족, 잠재버그, 복잡도, 문서화, 설계에 대한 품질척도 표시
 

# 6. 운영

## 6.1. Auto-Scaler Policy
Horizontal Pod Autoscaler를 추가 하여 CPU 사용률에 따라 Pod가 Scale Out 될 수 있도록 설정
````
(yml 현행화 필요)
apiVersion: autoscaling/v2beta2
kind: HorizontalPodAutoscaler
metadata:
  name: petmily-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: petmily-match
  minReplicas: 1
  maxReplicas: 2
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 50
status:
  observedGeneration: 1
  lastScaleTime: <some-time>
  currentReplicas: 1
  desiredReplicas: 1
  currentMetrics:
  - type: Resource
    resource:
      name: cpu
      current:
        averageUtilization: 0
        averageValue: 0
...        
````

## 6.2. 블루-그린 배포
기존의 Rolling 배포 방식을 배포 시간 동안의 버전 Compatibility 문제와 빠른 Rollback을 위해 Blue-Green으로 배포 전략을 변경
````
apiVersion: v1
kind: Service
metadata:
  name: test-service
spec:
  ports:
    - port: 80
      targetPort: 8080
  selector:
    app: test-pod
    color: blue
    
(yml 현행화 필요)
apiVersion: apps/v1
kind: Deployment
metadata:
  name : test-deployment-blue
spec:
  replicas: 4
  selector:
    matchLabels:
      app: test-pod
      color: blue
  template:
    metadata:
      labels:
        app: test-pod
        color: blue
    spec:
      containers:
        - name: test-pod
          image: joont92/echo-version:0.1.0
          ports:
            - containerPort: 8080

apiVersion: apps/v1
kind: Deployment
metadata:
  name : test-deployment-green
spec:
  replicas: 4
  selector:
    matchLabels:
      app: test-pod
      color: green
  template:
    metadata:
      labels:
        app: test-pod
        color: green
    spec:
      containers:
        - name: test-pod
          image: joont92/echo-version:0.2.0 # 신버전
          ports:
            - containerPort: 8080

````
## 6.3 모니터링
Pod 내 Application을 모니터링 하기 위해 Spring Boot Actuator를 적용하고 Prometheus Exporter를 이용해 Customer Metric 정보를 수집하고
이를 통해 HPA를 통해 AutoScale 설정을 적용
````
Pom.xml
        <!-- Heath Check -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Prometheus  -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-core</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>

Security Config
...
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.cors()
				.and()
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
...
				.authorizeRequests()
				.antMatchers("/actuator/**").permitAll()
				.antMatchers("/api/match/**").access("hasRole('ROLE_USER')")
				.anyRequest()
				.authenticated();


Deployment - patch
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sample-spring-boot-on-kubernetes-deployment
spec:
  selector:
    matchLabels:
      app: sample-spring-boot-on-kubernetes
  template:
    metadata:
      annotations:
        prometheus.io/path: /actuator/prometheus
        prometheus.io/scrape: "true"
        prometheus.io/port: "8380"
      labels:
        app: petmily-match
    spec:

````
Grafana의 Promethus Plugin을 통해 Metric 정보를 시각화 할 수도 있을 것 같다.

## 6.4 로깅
LogStash Appender를 사용해 Cluster내 설치 된 Elk Stack에 Log를 기록하고 ElasticSearch/Kibana를 통해 시각화 할 수 있음
````
pom.xml
        <!-- LogStash Appender-->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>
        <dependency>
            <groupId>net.logstash.logback</groupId>
            <artifactId>logstash-logback-encoder</artifactId>
        </dependency>
        
logback-spring.xml
    <!-- logstash -->
    <appender name="logstash" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>${logstash-host}:${logstash-port}</destination>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"applicationName":"${applicationName}"}</customFields>
        </encoder>
    </appender>

    <logger name="com.petmily.match" level="info" additivity="false">
        <appender-ref ref="console"/>
        <appender-ref ref="file"/>
        <appender-ref ref="logstash"/>
    </logger>

````
