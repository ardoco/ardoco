import os

#ATTENTION!!!! may create wrong splits (after e.g. and after i.e. ) (in our case: (only) for teammates)


def split_on_dot_space(text):
    text = ' '.join(text.splitlines())
    parts = text.strip().split('. ')

    sentences = []
    for i, part in enumerate(parts):
        part = part.strip()
        if not part:
            continue

        if i < len(parts) - 1:
            sentences.append(part + '.')
        else:
            if part[-1:] in '.!?':
                sentences.append(part)
            else:
                sentences.append(part)

    return sentences

def process_file():
    txt_files = [f for f in os.listdir('.') if f.endswith('.txt') and not f.endswith('_1SentPerLine.txt')]

    if len(txt_files) != 1:
        print("Fehler: Es darf genau eine .txt-Datei im Verzeichnis geben (außer _1SentPerLine.txt).")
        return

    input_file = txt_files[0]
    output_file = input_file.replace('.txt', '_1SentPerLine.txt')

    with open(input_file, 'r', encoding='utf-8') as infile:
        text = infile.read()

    sentences = split_on_dot_space(text)

    with open(output_file, 'w', encoding='utf-8') as outfile:
        for i, sentence in enumerate(sentences):
            outfile.write(sentence)
            if i < len(sentences) - 1:
                outfile.write('\n')

    print(f"Fertig: '{input_file}' → '{output_file}'")

if __name__ == '__main__':
    process_file()
