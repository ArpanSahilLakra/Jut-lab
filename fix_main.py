import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

# I will just write a clean MainActivity.kt and replace the entire file, it's safer.
