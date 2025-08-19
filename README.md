## Überblick
Hero2Zero – kleine Jakarta-EE-Anwendung (JSF + REST + JPA) zur Anzeige und Pflege von CO₂-Emissionsdaten.

## Tech-Stack
JDK 17, Payara 6 (Jakarta EE 10), JPA (MySQL), JSF (PrimeFaces).

## Screenshots (Umsetzung)
### Startseite – `emissions.xhtml`
Auswahl der Staatsbürgerschaft. Tabelle zeigt freigegebene CO₂-Einträge.
<p>
  <img src="docs/img/emissions.png" alt="Emissionsseite" width="800">
</p>

### Login – `login.xhtml`
Formularbasierter Login (Container-Security); Weiter zur `/admin/emissions-edit.xhtml` nach erfolgreicher Anmeldung.
<p>
  <img src="docs/img/login.png" alt="Login" width="800">
</p>

### Datenpflege (Wissenschaftsbereich) – `/admin/emissions-edit.xhtml` als eingeloggter User
Änderung von CO²-Werten, anlegen neuer Datensätze; Filterfunktion für schnelle Eingrenzung.
<p>
  <img src="docs/img/emissions-edit-wissenschaftler.png" alt="Emissions-Edit (Wissenschaftler)" width="800">
</p>

### Datenpflege (Wissenschaftsbereich) – `/admin/emissions-edit.xhtml` eingeloggt als Rolle 'admin'
Ändern, neu anlegen, freigeben oder löschen von Datensätzen; Filterfunktion wie User
<p>
  <img src="docs/img/emissions-edit-admin.png" alt="Emissions-Edit (Admin)" width="800">
</p>

## Architektur – Klassen (Kurzüberblick)
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

## Architektur – Komponenten
```mermaid
flowchart TB
  subgraph JSF["Frontend (JSF)"]
    index["index.xhtml"];
    emissions["emissions.xhtml"];
    admin["/admin/emissions-edit.xhtml"];
    login["login.xhtml"];
  end

  subgraph Beans["Managed Beans (CDI/JSF)"]
    EmissionBean;
    EmissionsAdminBean;
    LoginBean;
    HelloBean;
    FilterUtil;
  end

  subgraph REST["REST API (/api)"]
    EmissionResource;
    JakartaEE10Resource;
  end

  subgraph Persistence["Persistence / Daten"]
    EmissionDAO;
    CountryEmission;
    DB["MySQL hero2zero"];
  end

  %% Verbindungen
  index --> HelloBean;
  emissions --> EmissionBean;
  emissions --> LoginBean;
  admin --> EmissionsAdminBean;
  admin --> FilterUtil;
  admin --> LoginBean;

  EmissionBean --> EmissionDAO;
  EmissionsAdminBean --> EmissionDAO;
  EmissionResource --> EmissionDAO;

  EmissionDAO --> CountryEmission;
  EmissionDAO --> DB;
```
*Legende:* `-->` nutzt/ruft an. EL-Bindings aus XHTML → Beans sind hier **absichtlich** sichtbar; reine Container-Entdeckungen (z. B. `@ApplicationPath`) erzeugen keine Kante. </br>
EmissionResource → EmissionDAO = methodischer Aufruf (z. B. DAO-Methoden).</br>
EmissionDAO → CountryEmission/DB = JPA-Entity bzw. Datenbankzugriff.

## Nutzerflüsse – Sequenzen

### Navigation (Login/Logout/Admin)
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

### „Alle Daten“ auf *emissions.xhtml*
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

### Admin-Aktionen auf */admin/emissions-edit.xhtml*
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

## Setup und Run (Dev)
- Payara 6 starten; Datasource **`jdbc/hero2zero`** anlegen.  
- `persistence.xml` (PU: **Hero2ZeroPU**) zeigt auf  
  `jdbc:mysql://127.0.0.1:3306/hero2zero`, User `h2zuser`, PW `h2zpass`.  
- WAR deployen; App-URL entsprechend deiner Umgebung aufrufen.

## REST-API
`GET /api/emissions` – liefert `List<CountryEmission>` (siehe `@ApplicationPath("api")` + `@Path("emissions")`).

## Hinweise / Qualität
- JSF-EL erzeugt keine Klassenpfeile → im Komponentendiagramm visualisiert.
- Dev-Credentials nur lokal; produktiv via Env/Secrets.
- Sprechende Namen, kleine Klassen, klare Verantwortungen.
- Optional: weitere Details in CONTRIBUTING.md / SECURITY.md.
 
