
import pandas as pd
import json
import os

# === Einstellungen ===
input_path = 'D:/Projekte/hero2zero/Data/alle_laender.csv'
output_path = 'D:/Projekte/hero2zero/Data/kontinente.sql'
mapping_path = 'D:/Projekte/hero2zero/Data/continent_mapping.json'

# === Zuordnungsmöglichkeiten ===
continent_choices = {
    "1": "Afrika",
    "2": "Asien",
    "3": "Europa",
    "4": "Nordamerika",
    "5": "Suedamerika",
    "6": "Ozeanien",
    "7": "Antarktis"
}

# === Mapping laden oder initialisieren ===
if os.path.exists(mapping_path):
    with open(mapping_path, 'r', encoding='utf-8') as f:
        continent_mapping = json.load(f)
else:
    continent_mapping = {}

# === CSV einlesen ===
df = pd.read_csv(input_path, sep=';')
sql_lines = []

for country in df['country'].dropna().unique():
    continent = continent_mapping.get(country)
    if not continent:
        print(f'Kontinent für "{country}" nicht gefunden.')
        for key, name in continent_choices.items():
            print(f'{key} = {name}')
        choice = input('Wähle Kontinent (Zahl eingeben): ').strip()
        continent = continent_choices.get(choice, 'Unbekannt')
        continent_mapping[country] = continent

    sql_lines.append(
        f"UPDATE countryemission SET continent = '{continent}' WHERE country = '{country}';"
    )

# SQL-Datei speichern
with open(output_path, 'w', encoding='utf-8') as f:
    f.write('\n'.join(sql_lines))

# Mapping speichern
with open(mapping_path, 'w', encoding='utf-8') as f:
    json.dump(continent_mapping, f, indent=4, ensure_ascii=False)

print(f'Fertig: {len(sql_lines)} SQL-Zeilen gespeichert in {output_path}')
