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
        - [Auto-Scaler Policy](#6.1-Auto-Scaler-Policy)
        - [블루-그린 배포](#6.2-블루-그린-배포)
        - [모니터링](#6.3-모니터링)
        - [로깅](#6.4-로깅)

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
1. 성능
    1. 회원이 돌봄 상태를 실시간으로 조회할 수 있어야 한다.  


# 2. 체크포인트

- 분석 설계


- Microservice Outer/Inner Architecture 수립:
    - Microservice based Architecutre 구성 요소 종류와 요소 간 관계를 설명할 수 있는가?
        (API G/W, BFF, Core MS, Base MS, CI/CD, ACL 등)
    - 구성 요소들의 내부 구조를 설명할 수 있으며, 데이터 접근 전략 수립 및 퍼블리싱이
        가능한가?(헥사고널 아키텍처, 레이어드아키텍처, ORM, Restful API 설계 원칙/성숙도 등)
- Microservice 기반(Base, Backing)서비스 활용
    -  Biz Microservice가 원활하게 서비스하게 해주는 기반 서비스 및 Backing 서비스의
        구축/활용이 가능한가? (Spring Cloud Service 활용 : Eureka, Hystrix, Config 등)
    - 다양한 Language를 사용해야 하는 마이크로서비스 아키텍처에서는 Spring Cloud만
        이용하면 Java 외 언어를 지원할 수가 없는데, Polyglot 관점에서 어떻게 해야 하는가?      
- BIZ Microservice 식별
    - BIZ Microservice를 응집성 높고, 의존성이 낮도록 식별하여, 독립적 수정/배포
        할 수 있는가? (도메인 주도설계 : Bounded Context/Context Map)
    - 마이크로서비스를 분리하는 기준은 도메인 주도 설계뿐인가? 그 외적인 부분은 없는가?
        (도메인 주도 설계 외에 다른 관점에서 서비스를 식별한다면?)
    - 도메인 모델 패턴과 데이터 모델 패턴의 차이점을 정확히 이해하고 있는가?
- BiZ Microservice 상세 설계
    - BIZ Microservice의 내부구조의 각 레이어의 역할에 맞도록 비즈 기능을
        설계할 수 있는가? (API 설계, 데이터 모델링, 객체모델링, 외부 연계 등) 
- 서비스 API 설계
    - RESTful API에 대해 이해하고 있는가? 서비스별 API를 설계 할 수 있는가?
    - 마이크로서비스가 모든 환경에 적합한 것은 아닌데, 이를 구분하는 점은 무엇인가?    
- Data Considency(데이터 일관성)전략 수립
    - 마이크로서비스에서 DB를 분할하기 위한 방법을 제시하고 보완책을 설계할 수 있는가?
        - 비동기 이벤트 기반 SAGA패턴, 이벤트소싱 & CQRS 지식을 바탕으로
          보완책 설계 가능 여부
          (Polyglot Database, Event Sourcing/CQRS, EDA, SAGA, RabbitMQ, 카프카 등)
- 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?

- 구현
    - 환경 설정 
        -  Java의 경우 Maven or Gradle로 마이크로 서비스의 요건에 따라 개발 환경을
            설정할 수 있는가?
    - Container
        - Docker, Kubernetes, Jenkins를 사용하여 배포 할 수 있으며 단계별로 설명이 가능한가?
        - 각 단계마다 어플리케이션 구현 시 고려해야 할 사항은 무엇인가?
        - Kubernetes Ingress Controller를 이해하고 Load Balancer와 연동하여 활용할 수 있는가?
    - 개발 패턴  
        - MVC, MVVM의 차이점에 대해서 이해하고 있는가?
    - 데이터 핸들링  
        - 어플리케이션에서 데이터베이스를 핸들링할때 주로 사용될 수 있는 ORM과
          각 ORM 마다의 장단점을 명확하게 이해하고 있는가? 
    - 환경 이해(Dev, Staging, Production)
        - 어플리케이션 개발 시 일반적으로 Dev, Staging, Production 환경에 따라 
          설정 정보들이 달라질 수 있는데, 이 경우 어떻게 해야 하는가?
    - Cloud App Back-End 구현
        - 정의된 BackEnd Architecutre에 맞도록 표준 샘플을 작성하고, 가이드 할 수 있는가?
            - 비즈 로직 개발, 인증/인가, 기반 서비스 연계, 대외연계, 저장소 처리 구현 가능 여부
              (Spring Boot, Spring Security, Spring Cloud Connector 활용 기반)
- 운영
    - Auto-Scaler Policy
        - 워크로드의 트래픽 패턴에 맞는 자동스케일링 정책을 지정하고,
            App. 특성(CPU vs Disk Centric 등)에 맞도록 CPU/Memory/Throughput 임계치를
            지정할 수 있는가?
    - 블루-그린 배포, CI/CD
        - Jenkins로 PaaS에 배포하는 Space별 배포 Job과 파이프라인을 구성할 수 있는가?
        - 블루그린 배포 또는 Canary 배포 패턴을 적용하여 배포를 설계하고 수행할 수 있는가?
    - 모니터링
        - 컨테이너 기반 환경에서 애플리케이션 인스턴스를 모니터링하고, Prometheus OSS를
          활용해 애플리케이션 인스턴스의 리소스(CPU, Memory, Disk, N/W) 성능 및 상태를
          확인할 수 있는가?
        - 모니터링과 연계된 Auto-Scaler Policy 구성이 가능한가?
    - 로깅
        - 컨테이너 기반 환경에서 애플리케이션 로그를 수집, 저장 및 분석 가능한가?
          (Fluent, Filebeat OSS를 활용하여 애플리케이션 로그 수집 & 수집 로그를
          ElasticSearch와 같은 저장소에 저장하고 Kibana등을 활용하여 분석 등)


# 3. 분석/설계


## 3.1 조직 (Scrum Team Board)
![image](![3_1 ScrumTeam Board](https://user-images.githubusercontent.com/67447558/126139765-1a5d6d27-9db0-47bb-8449-70895e4dabcd.jpg)
- 스크럼 팀은 관리자가 없으며 자율적, 주도적으로 일하는 조직으로 구성

## 3.2 Team Project Vision
![3_2 Vision](https://user-images.githubusercontent.com/67447558/126139848-c53230a7-7e1b-4992-b0c1-0fdb736fce75.jpg)

- 스크럼 팀 프로젝트 Petmily의 Vision 정의

## 3.3 Event Storming 결과
![3_3 eventstorming](https://user-images.githubusercontent.com/67447558/126139885-72c0afe7-3ec1-40bb-b06b-31616c6764e1.png)

    - DDD(Domain Driven Design) 도메인 주도 설계 통한 마이크로서비스 식별
        - Domain Event, Hot Spot, Command, Actor, Entity, Aggregate 찾기
        - 도메인 모델을 구분하는 경계인 Bounded Context (한정된 문맥) 찾기
        - MicroService(마이크로 서비스) 식별
        - Service Mapping Diagram(서비스 매핑 다이어그램) 도출
        - 각 MSA 서비스별 Service Specification(서비스 스펙) 작성
### Key Concept

![3_3_1 KeyConcept](https://user-images.githubusercontent.com/67447558/126139973-ffebe2b8-9ef8-41d0-96ab-4c750f4704fb.png)

    - Event Storming 통한 마이크로 서비스 식별 및 Key Concept 도출


### 서비스 매핑 다이어그램

![3_3_2 MappingDiagram](https://user-images.githubusercontent.com/67447558/126139997-559aa042-c1c1-4043-9a77-4247d736e13d.png)

    - API Gateway로 유입되는 요청 및 마이크로 서비스 간 매핑 다이어그램
        - API Gateway 유입 요청에 대한 인증 정보 확인 후 각 마이크로 서비스로 라우팅
        - User, Pet, Match, Diary, Assessment 서비스는 기본적으로 메시지 큐(Kafka)통해 이벤트 기반 Pub/Sub 방식으로 통신
        - 실시간 조회 필요 시 REST 기반 Sync 직접 호출 수행

## 3.4 시나리오 기능적/비기능적 요구사항을 커버 검증

![3_4 ScenarioModel](https://user-images.githubusercontent.com/67447558/126140026-8d9a7648-a3e4-4321-8315-157ad4388c40.JPG)

    - 기능적 요구사항 커버 검증
        - 시터가 회원이 맡기기 원하는 애완동물을 선택하여 매칭 요청을 한다. (ok)
        - 회원과 시터가 각자의 정보를 며 매칭을 승인하거나 거절한다. (ok)
        - 회원과 시터가 서로 매칭되고나면 애완동물의 돌봄이 시작된다. (ok)
        - 시터가 돌보고 있는 애완동물의 일지를 작성한다. (ok)
        - 회원이 돌봄이 끝난 시터를 평가한다. (ok)
        - 시터가 돌봄이 끝난 애완동물을 평가한다. (ok)
    - 비기능적 요구사항 커버 검증
        - 매칭 시 회원 정보와 애완동물 정보는 실시간으로 조회해서 보여줘야 한다. Sync 호출 (ok)
        - 매칭 기능이 수행되지 않더라도 회원 등록 및 애완동물 등록 가능해야 한다. Async (event-driven), Eventual Consistency (ok)
        - 평가 기능이 수행되지 않더라도 사용자, 매칭, 애완동물, 일지 등록 기능은 정상 작동해야 한다. Async (event-driven), Eventual Consistency (ok)
        - 매칭 기능에 과부하가 걸리면 매칭을 잠시동안 진행하지 않고 잠시후에 하도록 유도한다. Circuit breaker, fallback (-)
        - 회원이 돌봄 상태를 실시간으로 조회할 수 있어야 한다. CQRS (-)

### 서비스 스펙 정의

![3_4_1 Design](https://user-images.githubusercontent.com/67447558/126140065-ce9cc0b3-a29f-46ea-8024-a2cb7bce2dc5.png)

    - 도출된 마이크로서비스 User, Pet, Match, Diary, Assessment 각 서비스별 도메인 모델링

## 3.5 헥사고날 아키텍처 다이어그램 도출

![3_5 Architecture](https://user-images.githubusercontent.com/67447558/126140088-6cbff6c4-81b1-404d-af8e-2f58660ac1c5.png)

    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


## 3.6 Outer/Inner Architecture
<img src="https://postfiles.pstatic.net/MjAyMTA3MTZfMTAw/MDAxNjI2MzY4MTQ0OTc2.N-i7XwIgqObq2tixuDgotFDOR0wdkSwOyq75XwoYcYcg.pgq6TyfzW3W2QDUpHv9TslMbaTVhTRzMDAekbFiM4bAg.JPEG.ttann/architecture.JPG?type=w966">

## 3.7 구현 패턴
###  3.7.1 Database per service 
###### - framework : mairaDB
###### - 각 마이크로 서비스별 분리된 Database를 사용한다 


| 서비스 | DB|
| -------- | -------- | 
| User    | factory-zdb-petdb-mariadb.factory-zdb:3306/petmily-user     |
| Pet    | factory-zdb-petdb-mariadb.factory-zdb:3306/petmily-pet     |
| Match    |factory-zdb-petdb-mariadb.factory-zdb:3306/petmily-match     |
| Diary    | factory-zdb-petdb-mariadb.factory-zdb:3306/petmily-diary     |
| Assess    | factory-zdb-petdb-mariadb.factory-zdb:3306/petmily-assess     |


### 3.7.2 Service Registry  & API Gateway 
###### - framework : Ingress
######  - 각 마이크로서비스는 독립된 dns url을 가지며, ingress에 설정된 route rule에 의해 트래픽 라우팅 된다. 
######  - 유형 : DNS 기반(kube-dns) 적용 
 
| 서비스 | DNS URL |
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
### 3.7.3 Client-side UI Composition
######  - framework  : react + MVVM 패턴
######  - front-end도 back-end 마이크로서비스처럼 기능별로 분리하고 이를 조합하기 위한 frame 형태의 부모창을 통해 각 front-end component를 조합하여 동작하게 한다. 부모서비스는 틀만 가지며 실제 각 기능표현은 front-end component가 구현하게 한다.  비스니스 규현을 위해 front-end는 여러개의 back-end 마이크로서비스 API를 호출한다
######  - 적용사례 : Petmily main 화면내 sitter, pet, 리뷰 조회는 별개의 front-end component가 수행하며, main화면은 화면의 틀을 구성한다
<img src="https://postfiles.pstatic.net/MjAyMTA3MTZfMjg3/MDAxNjI2MzY5NDg5ODEy.au99670qK10EDXuPdbVbnm2NdRm_Llxu8vmvhac92ksg.44LyJLgcJJ5WyuuGGmlcB8vDWYZu1W6fqVC0j1Vc06kg.JPEG.ttann/Client-side_UI_Composition.JPG?type=w966">



### 3.7.4 인증/인가 패턴
######  - framework  : Spring Security(JWT(JSON Web Token) 기반) + redis 
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

### 3.7.5 CQRS 패턴
######  - CQRS 패턴 구현을 위해 별도의 View Service를 설계 (미구현)
######  - 적용사례 : Pet 등록시, Match 승인시, 돌봄 종료시, Pet Diary 등록시 View Service에 비동기식 Event를 전송하여 View 데이터 저장
Spring 계열에서는 Axon Framework 를 많이 사용하는 것으로 보임. 각 App에서 발생하는 각각의 모든 Entity 들의 변경사항들에 대해 Topic내에 Event를 Produce
시키도록 하고 (현재 구현상태) 이 발생하는 Event를 별도의 System을 구축하여 Event 축적에 따른 성능저하를 실제 시스템에서는 줄일 수 있을 방법이 될 수 있을 것 같음
![image](https://factory-git.cloudzcp.io/attachments/99ede752-7427-4ae0-ba92-24655cc59500)


### 3.7.6 기타

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
Horizontal Pod Autoscaler를 추가 하여 CPU 사용률에 따라 Scale Out 될 수 있도록 설정
````
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
  maxReplicas: 10
  targetCPUUtilizationPercentage: 50
...        
````

## 6.2. 블루-그린 배포
기존의 Rolling 배포 방식을 배포 시간 동안의 버전 Compatibility 문제와 빠른 Rollback을 위해 Blue-Green으로 배포 전략을 변경
````
(Jenkins Script 변경 후 현행화 필요)
...
apiVersion: v1
kind: Service
metadata:
  name: petmily-match-blue
spec:
  selector:
    app: petmily-match
    color: blue
...
    
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
Spring Boot Actuator를 이용해 Prometheus Metric 정보를 수집 함
ZCP Cluster에 설치되어 있는 Prometheus 서버를 이용해 Customer Metric 정보를 수집하고 이를 통해 HPA를 통해 AutoScale 설정을 적용할 수 있음

http://petmily.factory-dev.cloudzcp.com/match/actuator/prometheus
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

````
Grafana의 Promethus Plugin을 통해 Metric 정보를 시각화 할 수도 있을 것 같다.

## 6.4 로깅
LogStash Appender를 사용해 Cluster내 설치 된 Elk Stack에 Log를 기록하고 ElasticSearch/Kibana를 통해 시각화 할 수 있음

https://factory-logging.cloudzcp.io/app/kibana#/discover/26d7f020-e537-11eb-91ac-8992604aeb6a?_g=(refreshInterval%3A(display%3AOff%2Cpause%3A!f%2Cvalue%3A0)%2Ctime%3A(from%3Anow%2Fd%2Cmode%3Aquick%2Cto%3Anow%2Fd))
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
