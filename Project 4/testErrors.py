import sys
from collections import defaultdict
import re

print('\nChecking compiler logs')

actual = []
inp: dict[int, list[str]] = defaultdict(list)
pattern = re.compile(r'^(\d+):\d+\s*[*]{4}ERROR[*]{4}\s*(.+)\s*$')

while True:
    line = sys.stdin.readline()
    if not line:
        break

    actual.append(line)
    if 'ERROR' not in line:
        continue

    if 'Syntax error' in line:
        print('Program contains syntax errors. Aborting!')
        print('First encountered error:', line, sep='\n')
        exit(-1)

    if match := pattern.match(line):
        linenum = int(match.group(1))
        inp[linenum].append(match.group(2))
    elif 'ERROR' in line:
        print('Unexpected error.')
        print(line)
    else:
        print('no match on "', line.strip(), '"', sep='')

errs_map = {
    1: 'Multiply-declared identifier',
    2: 'Undeclared identifier',
    3: 'Colon-access of non-tuple type',
    4: 'Invalid tuple field name',
    5: 'Non-function declared void',
    6: 'Invalid name of tuple type',
}

expected: dict[int, list[str]] = defaultdict(list)
exp_pattern = re.compile(r'^.*!!\s*(([1-6],?)+)\|.*$')

with open('nameErrors.base') as f:
    for linenum, line in enumerate(map(str.strip, f.readlines()), 1):
        if match := exp_pattern.match(line):
            errs = map(int, match.group(1).split(','))
            for err in errs:
                expected[linenum].append(errs_map[err])

inp = dict(inp)
expected = dict(expected)
success = True
unexpected: dict[int, list[str]] = defaultdict(list)
for key, value in expected.items():
    if key not in inp:
        e = 'errors' if len(value) > 1 else 'error'
        print(f'Expected {e} on line {key}: {value}')
        success = False
        continue

    act = inp[key]

    for v in value:
        if v not in act:
            print(f'Expected error {v} on line {key}')
            success = False

for key, value in inp.items():
    if key not in expected:
        e = 'errors' if len(value) > 1 else 'error'
        print(f'Unexpected {e} on line {key}: {value}')
        success = False
        continue

    act = expected[key]

    for v in value:
        if v not in act:
            print(f'Unexpected error {v} on line {key}')
            success = False

if success:
    print('All good!')
elif unexpected:
    print(f'Found unexpected errors. Relevant compiler logs')
    print(''.join(map(lambda x: f'{x[0]}: {x[1]}', unexpected.items())))
else:
    print(f'Some tests FAILED!')
