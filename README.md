## Überblick
Like Hero To Zero ist eine Jakarta-EE-Anwendung (JSF + REST + JPA) zur Anzeige und Pflege von CO₂-Emissionsdaten. Als Datenquelle wurde unter https://ourworldindata.org/co2-and-greenhouse-gas-emissions der 'Download full data' genutzt (includes all enities an time points).<br>
Es gibt drei Nutzerzustände: nicht eingeloggt (öffentlich), eingeloggt ohne Admin-Rolle (Wissenschaftler*in*) und eingeloggt mit Admin-Rolle (Administrator*in*).<br>
Dies ist ein Studienprojekt ohne kommerziellen Hintergrund.

## Tech-Stack
JDK 17, Payara 6 (Jakarta EE 10), Jakarta REST (JAX-RS), JPA 3.0 (MySQL), JSF (PrimeFaces).

## Screenshots
### Startseite – `emissions.xhtml`
Auswahl des Landes (der Staatsbürgerschaft). Tabelle zeigt freigegebene CO₂-Einträge.
<p>
  <img src="docs/img/emissions.png" alt="Emissionsseite" width="400">
</p>

### Login – `login.xhtml`
Formularbasierter Login (Container-Security); nach erfolgreicher Anmeldung automatische Weiterleitung auf `/admin/emissions-edit.xhtml`.
<p>
  <img src="docs/img/login.png" alt="Login" width="400">
</p>

### Datenpflege (Wissenschaftsbereich) – `/admin/emissions-edit.xhtml` als eingeloggter User
Änderung von CO₂-Werten, anlegen neuer Datensätze; Filterfunktion für schnelle Eingrenzung.
<p>
  <img src="docs/img/emissions-edit-wissenschaftler.png" alt="Emissions-Edit (Wissenschaftler)" width="400">
</p>

### Datenpflege (Wissenschaftsbereich) – `/admin/emissions-edit.xhtml` eingeloggt als Rolle 'admin'
Ändern, neu anlegen, freigeben oder löschen von Datensätzen; Filterfunktion wie User
<p>
  <img src="docs/img/emissions-edit-admin.png" alt="Emissions-Edit (Admin)" width="400">
</p>

## Architektur – Klassen (Kurzüberblick)
```mermaid
classDiagram
  %% Associations via fields
  EmissionBean --> EmissionDAO : field (dao)
  EmissionsAdminBean --> EmissionDAO : field (dao)
  EmissionResource --> EmissionDAO : field (dao)
  EmissionBean --> CountryEmission : field (latestEmission)
  EmissionsAdminBean --> CountryEmission : field (newEmission)

  %% Dependencies via method signatures
  EmissionDAO ..> CountryEmission : returns/params
  EmissionBean ..> CountryEmission : returns
  EmissionsAdminBean ..> CountryEmission : returns/params
  EmissionResource ..> CountryEmission : returns
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
*Legende:* `-->` nutzt/ruft an. EL-Bindings aus XHTML → Beans sind hier **absichtlich** sichtbar; reine Container-Entdeckungen (z. B. `@ApplicationPath`) erzeugen keine Kante. <br>
EmissionResource → EmissionDAO = methodischer Aufruf (z. B. DAO-Methoden).<br>
EmissionDAO → CountryEmission/DB = JPA-Entity bzw. Datenbankzugriff.

## Nutzerflüsse – Sequenzen

### Navigation (Login/Logout/Admin)
```mermaid
sequenceDiagram
  actor User
  participant EmissionsPage as "emissions.xhtml"
  participant LoginPage as "login.xhtml"
  participant AdminPage as "/admin/emissions-edit.xhtml"
  participant LoginBean

  alt not logged in
    User ->> EmissionsPage: Klick "Login" (outcome="login")
    EmissionsPage ->> LoginPage: GET login.xhtml
    User ->> LoginPage: Submit loginForm (action="j_security_check")
    LoginPage ->> AdminPage: redirect /admin/emissions-edit.xhtml (on success)
  else already logged in
    User ->> EmissionsPage: Klick "Datenpflege"
    EmissionsPage ->> AdminPage: GET /admin/emissions-edit.xhtml
  end

  User ->> AdminPage: Klick "Logout"
  AdminPage ->> LoginBean: logout()
  LoginBean ->> EmissionsPage: redirect "/emissions.xhtml"
```

### „Alle Daten“ auf *emissions.xhtml*
```mermaid
sequenceDiagram
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
  EmissionsPage -->> User: Tabelle aktualisiert (freigegebene Einträge)
```

### Admin-Aktionen auf */admin/emissions-edit.xhtml*
Dieses Sequenzdiagramm zeigt die drei zustandsändernden Admin-Use-Cases (Freigabe, Löschen, Speichern). Pfad: JSF-Seite → EmissionsAdminBean → EmissionDAO → DB; danach Listenaktualisierung im UI. Zugriff ist auf die Rolle 'admin' beschränkt.

```mermaid
sequenceDiagram
  actor Admin
  participant AdminPage as "/admin/emissions-edit.xhtml"
  participant EmissionsAdminBean
  participant EmissionDAO
  participant DB as "MySQL hero2zero"

  opt Freigabe (nur Admin)
    Admin ->> AdminPage: Klick "Freigabe"
    AdminPage ->> EmissionsAdminBean: approve(e)
    EmissionsAdminBean ->> EmissionDAO: update(e)
    EmissionDAO ->> DB: JPA merge
    DB -->> EmissionDAO: OK
    EmissionDAO -->> EmissionsAdminBean: done
    EmissionsAdminBean -->> AdminPage: Listen aktualisieren
  end

  opt Datensatz löschen (nur Admin)
    Admin ->> AdminPage: Klick Trash-Icon (Datensatz löschen)
    AdminPage ->> EmissionsAdminBean: delete(e)
    EmissionsAdminBean ->> EmissionDAO: delete(e)
    EmissionDAO ->> DB: JPA remove
    DB -->> EmissionDAO: OK
    EmissionDAO -->> EmissionsAdminBean: done
    EmissionsAdminBean -->> AdminPage: Listen aktualisieren
  end

  opt Neu anlegen speichern (Dialog)
    Admin ->> AdminPage: Klick "Speichern"
    AdminPage ->> EmissionsAdminBean: saveNew()
    EmissionsAdminBean ->> EmissionDAO: create(newEmission)
    EmissionDAO ->> DB: JPA persist
    DB -->> EmissionDAO: OK
    EmissionDAO -->> EmissionsAdminBean: done
    EmissionsAdminBean -->> AdminPage: Listen aktualisieren
  end
```

**Hinweis zur Persistenz:** In diesem Repo sind keine Zugangsdaten hinterlegt. 
Bitte `persistence.example.xml` nach `META-INF/persistence.xml` kopieren und die Platzhalter ausfüllen:

```xml
<property name="jakarta.persistence.jdbc.driver"   value="com.mysql.cj.jdbc.Driver"/>
<property name="jakarta.persistence.jdbc.url"      value="jdbc:mysql://127.0.0.1:3306/hero2zero"/>
<property name="jakarta.persistence.jdbc.user"     value="PUT_DBUSER_HERE"/>
<property name="jakarta.persistence.jdbc.password" value="PUT_DBPASSWORT_HERE"/>
```

## REST-API
`GET /api/emissions` – liefert `List<CountryEmission>` 

### Sicherheit
Echte DB-Zugangsdaten werden nicht im Repo gespeichert; `persistence.xml` ist lokal zu pflegen.

## Hinweise / Qualität
- JSF-EL erzeugt keine Klassenpfeile → im Komponentendiagramm visualisiert.
- Dev-Credentials nur lokal; produktiv via Env/Secrets.
- Sprechende Namen, kleine Klassen, klare Verantwortungen.
 
