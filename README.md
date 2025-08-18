## Überblick
Hero2Zero – kleine Jakarta-EE-Anwendung (JSF + REST + JPA) zur Anzeige und Pflege von CO₂-Emissionsdaten.

## Tech-Stack
JDK 17, Payara 6 (Jakarta EE 10), JPA (MySQL), JSF (PrimeFaces).

## Architektur (Kurzüberblick – Klassen)

```mermaid
classDiagram
  %% Associations via fields
  EmissionBean --> EmissionDAO : field (dao)
  EmissionsAdminBean --> EmissionDAO : field (dao)
  EmissionResource --> EmissionDAO : field (dao)
  EmissionDAO --> CountryEmission : manages
  EmissionBean --> CountryEmission : field (latestEmission)
  EmissionsAdminBean --> CountryEmission : field (newEmission)

  %% Dependencies via method signatures
  EmissionDAO ..> CountryEmission : returns/params
  EmissionBean ..> CountryEmission : returns
  EmissionsAdminBean ..> CountryEmission : returns/params
```
```mermaid
flowchart TB
  %% --- Cluster ---
  subgraph JSF["Frontend (JSF)"];
    index["index.xhtml"];
    emissions["emissions.xhtml"];
    admin["/admin/emissions-edit.xhtml"];
    login["login.xhtml"];
  end;

  subgraph Beans["Managed Beans (CDI/JSF)"];
    EmissionBean;
    EmissionsAdminBean;
    LoginBean;
    HelloBean;
    FilterUtil;
  end;

  subgraph REST["REST API (/api)"];
    EmissionResource;
    JakartaEE10Resource;
  end;

  subgraph Persistence["Persistence / Daten"];
    EmissionDAO;
    CountryEmission;
    DB["MySQL hero2zero"];
  end;

  %% --- Verbindungen (aus deinem Code) ---
  index --> HelloBean;
  emissions --> EmissionBean;
  emissions --> LoginBean;
  admin --> EmissionsAdminBean;
  admin --> FilterUtil;
  login --> LoginBean;

  EmissionBean --> EmissionDAO;
  EmissionsAdminBean --> EmissionDAO;
  EmissionResource --> EmissionDAO;

  EmissionDAO --> CountryEmission;
  EmissionDAO --> DB;
```

```mermaid
sequenceDiagram
  autonumber
  actor User
  participant EmissionsPage as "emissions.xhtml"
  participant LoginPage as "login.xhtml"
  participant AdminPage as "/admin/emissions-edit.xhtml"
  participant LoginBean

  alt not logged in
    User ->> EmissionsPage: Klick "Login"
    EmissionsPage ->> LoginPage: GET login.xhtml
  end
  User ->> LoginPage: Submit j_security_check
  LoginPage ->> EmissionsPage: GET emissions.xhtml
  alt logged in
    User ->> EmissionsPage: Klick "Datenpflege"
    EmissionsPage ->> AdminPage: GET /admin/emissions-edit.xhtml
  end
  User ->> EmissionsPage: Klick "Logout"
  EmissionsPage ->> LoginBean: logout()
  LoginBean ->> EmissionsPage: redirect "/emissions.xhtml"
```

```mermaid
sequenceDiagram
  autonumber
  actor User
  participant EmissionsPage as "emissions.xhtml"
  participant LoginPage as "login.xhtml"
  participant AdminPage as "/admin/emissions-edit.xhtml"
  participant LoginBean

  alt not logged in (#{loginBean.loggedIn} == false)
    User ->> EmissionsPage: Klick "Login" (outcome="login")
    EmissionsPage ->> LoginPage: GET login.xhtml
  end

  User ->> LoginPage: Submit loginForm (action="j_security_check")
  LoginPage ->> User: Auth-Result (Container)
  User ->> LoginPage: Klick "Home" (outcome="emissions")
  LoginPage ->> EmissionsPage: GET emissions.xhtml

  alt logged in (#{loginBean.loggedIn} == true)
    User ->> EmissionsPage: Klick "Datenpflege" (outcome="/admin/emissions-edit.xhtml")
    EmissionsPage ->> AdminPage: GET /admin/emissions-edit.xhtml
  end

  User ->> EmissionsPage: Klick "Logout" (action="#{loginBean.logout()}")
  EmissionsPage ->> LoginBean: logout()
  LoginBean ->> EmissionsPage: redirect "/emissions.xhtml"

```

```mermaid
sequenceDiagram
  autonumber
  actor User
  participant EmissionsPage as "emissions.xhtml"
  participant EmissionBean
  participant EmissionDAO
  participant DB as "MySQL hero2zero"

  User ->> EmissionsPage: Klick "Alle Daten"
  EmissionsPage ->> EmissionBean: ladeAlle()
  EmissionBean ->> EmissionDAO: findAllApproved()
  EmissionDAO ->> DB: JPA-Query (CountryEmission, approved=true)
  DB -->> EmissionDAO: List<CountryEmission>
  EmissionDAO -->> EmissionBean: List<CountryEmission>
  EmissionBean -->> EmissionsPage: update dataTable (AJAX)
  EmissionsPage -->> User: Tabelle mit freigegebenen Einträgen
```

```mermaid
sequenceDiagram
  autonumber
  actor Admin
  participant AdminPage as "/admin/emissions-edit.xhtml"
  participant EmissionsAdminBean
  participant EmissionDAO
  participant DB as "MySQL hero2zero"

  alt Approve
    Admin ->> AdminPage: Klick "Approve"
    AdminPage ->> EmissionsAdminBean: approve(e)
    EmissionsAdminBean ->> EmissionDAO: update(e)
    EmissionDAO ->> DB: JPA merge
    DB -->> EmissionDAO: OK
    EmissionDAO -->> EmissionsAdminBean: done
    EmissionsAdminBean -->> AdminPage: Refresh Listen
  else Delete
    Admin ->> AdminPage: Klick "Delete"
    AdminPage ->> EmissionsAdminBean: delete(e)
    EmissionsAdminBean ->> EmissionDAO: delete(e)
    EmissionDAO ->> DB: JPA remove
    DB -->> EmissionDAO: OK
    EmissionDAO -->> EmissionsAdminBean: done
    EmissionsAdminBean -->> AdminPage: Refresh Listen
  else Save New
    Admin ->> AdminPage: Klick "Speichern"
    AdminPage ->> EmissionsAdminBean: saveNew()
    EmissionsAdminBean ->> EmissionDAO: create(newEmission)
    EmissionDAO ->> DB: JPA persist
    DB -->> EmissionDAO: OK
    EmissionDAO -->> EmissionsAdminBean: done
    EmissionsAdminBean -->> AdminPage: Refresh Listen
  end
```
