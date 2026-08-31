import os
import re

def find_missing_imports(src_dir):
    missing_files = []
    
    # regex to find import statements like: import { X } from '@/features/...'
    # or import X from '@/features/...'
    import_pattern = re.compile(r"import\s+(?:{\s*([A-Za-z0-9_]+)\s*}|([A-Za-z0-9_]+))\s+from\s+['\"]@/features/(.*?)['\"]")

    for root, dirs, files in os.walk(src_dir):
        for file in files:
            if file.endswith(('.tsx', '.ts')):
                file_path = os.path.join(root, file)
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                    matches = import_pattern.findall(content)
                    for match in matches:
                        # match[0] is { X }
                        # match[1] is X (default)
                        # match[2] is path
                        name = match[0] if match[0] else match[1]
                        path = match[2]
                        
                        expected_base = os.path.join("src/features", path)
                        
                        expected_ts = expected_base + ".ts"
                        expected_tsx = expected_base + ".tsx"
                        expected_index_ts = os.path.join(expected_base, "index.ts")
                        expected_index_tsx = os.path.join(expected_base, "index.tsx")
                        
                        if not any(os.path.exists(p) for p in [expected_ts, expected_tsx, expected_index_ts, expected_index_tsx]):
                            missing_files.append((expected_base, name))
                            
    return list(set(missing_files))

def create_mock(expected_base, name):
    is_service = "service" in expected_base
    ext = ".ts" if is_service else ".tsx"
    
    full_path = expected_base + ext
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    
    with open(full_path, 'w', encoding='utf-8') as f:
        if is_service:
            f.write(f"export const {name} = {{}};\n")
        else:
            f.write(f"export const {name} = () => <div className='p-4 border rounded bg-white mt-4'>{name} (Mock)</div>;\n")
            
    print(f"Created: {full_path}")

if __name__ == "__main__":
    missing = find_missing_imports("src")
    for m in missing:
        create_mock(m[0], m[1])
    print("Done finding and creating missing imports.")
