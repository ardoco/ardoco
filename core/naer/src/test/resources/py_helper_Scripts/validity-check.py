import os
import csv

def find_text_file(base_dir='.'):
    for root, dirs, files in os.walk(base_dir):
        if 'text' in os.path.basename(root).lower():
            for file in files:
                if '1sentperline' in file.lower() and file.lower().endswith('.txt'):
                    return os.path.join(root, file)
    return None

def validate_ner(csv_path, txt_path):
    with open(txt_path, 'r', encoding='utf-8') as f:
        sentences = [line.strip() for line in f.readlines()]

    invalid_entries = []

    with open(csv_path, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            try:
                sentence_index = int(row['sentence'])-1 #-1 because its 1 indexed
                component = row['componentName']
                if sentence_index >= len(sentences)-1 or component.lower() not in sentences[sentence_index].lower():
                    invalid_entries.append((row, sentences[sentence_index] if sentence_index < len(sentences) else '[OUT OF BOUNDS]'))
            except (ValueError, KeyError) as e:
                invalid_entries.append((row, '[INVALID ROW FORMAT]'))

    if not invalid_entries:
        print("VALID")
    else:
        print(f"INVALID ENTRIES FOUND: ({len(invalid_entries)}/{len(sentences)})\n")
        invalid_components = set()
        for entry, sentence in invalid_entries:
            print(f"CSV Entry: {entry}")
            print(f"Sentence: {sentence}")
            print("-" * 50)
            invalid_components.add(entry['componentName'])
            invalid_components.add(component)

        print("Distinct invalid component names:")
        for comp in sorted(invalid_components):
            print(comp)

if __name__ == '__main__':
    csv_path = 'goldstandard_NER.csv'
    txt_path = find_text_file()

    if not txt_path:
        print("Text file not found.")
    elif not os.path.exists(csv_path):
        print("CSV file not found.")
    else:
        validate_ner(csv_path, txt_path)
