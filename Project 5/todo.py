import re

with open('ast.java') as f:
    src = ''.join(f.readlines()[:80])


pattern = re.compile(r'\s-(\w+)')

todo = pattern.findall(src)

if todo:
    print(f'{len(todo)} more classes to do.')
    print(*todo, sep=', ')
