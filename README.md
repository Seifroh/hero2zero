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
