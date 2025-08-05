import os
import json

def format_json_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        try:
            json_content = json.load(file)
            formatted_content = json.dumps(json_content, indent=2)
            with open(file_path, 'w', encoding='utf-8') as output_file:
                output_file.write(formatted_content)
            print(f"Formatted: {file_path}")
        except json.JSONDecodeError as e:
            print(f"Error decoding JSON in {file_path}: {e}")

def format_acm_files(directory):
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.acm'):
                file_path = os.path.join(root, file)
                format_json_file(file_path)

# Use the current directory
directory_path = '.'

# Call the function to format .acm files in the current directory
format_acm_files(directory_path)
