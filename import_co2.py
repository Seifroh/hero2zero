import csv
import mysql.connector

conn = mysql.connector.connect(
    host="localhost",
    user="h2zuser",
    password="h2zpass",
    database="hero2zero"
)
cursor = conn.cursor()

with open("D:/Projekte/hero2zero/Data/clean-co2.csv", newline='', encoding='utf-8') as csvfile:
    reader = csv.DictReader(csvfile, delimiter=';')
    for i, row in enumerate(reader, start=2):  # start=2 wegen Kopfzeile
        try:
            cursor.execute(
                "INSERT INTO countryemission (country, country_code, year, co2_emissions) VALUES (%s, %s, %s, %s)",
                (
                    row.get("country"),
                    row.get("country_code") or None,
                    int(row["year"]) if row["year"] else None,
                    float(row["co2_emissions"].replace(",", ".")) if row["co2_emissions"] else None
                )
            )
        except Exception as e:
            print(f"Fehler in Zeile {i}: {e}")

conn.commit()
cursor.close()
conn.close()

print("Import abgeschlossen.")
