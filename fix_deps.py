import re

with open('app/build.gradle.kts', 'r') as f:
    text = f.read()

deps_to_remove = [
    "implementation(libs.coil.compose)",
    "implementation(libs.converter.moshi)",
    "implementation(libs.moshi.kotlin)",
    "implementation(libs.retrofit)",
    "implementation(libs.logging.interceptor)",
    '  "ksp"(libs.moshi.kotlin.codegen)'
]

for dep in deps_to_remove:
    text = text.replace(dep, f"// {dep.strip()}")

with open('app/build.gradle.kts', 'w') as f:
    f.write(text)

