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
    1: 'Write attempt of function name',
    2: 'Write attempt of tuple name',
    3: 'Write attempt of tuple variable',
    4: 'Write attempt of void',
    5: 'Read attempt of function name',
    6: 'Read attempt of tuple name',
    7: 'Read attempt of tuple variable',
    8: 'Call attempt on non-function',
    9: 'Function call with wrong # of args',
    10: 'Actual type does not match formal type',
    11: 'Return value missing',
    12: 'Return with value in void function',
    13: 'Return value wrong type',
    14: 'Arithmetic operator used with non-integer operand',
    15: 'Relational operator used with non-integer operand',
    16: 'Logical operator used with non-logical operand',
    17: 'Non-logical expression used in if condition',
    18: 'Non-logical expression used in while condition',
    19: 'Mismatched type',
    20: 'Equality operator used with void function calls',
    21: 'Equality operator used with function names',
    22: 'Equality operator used with tuple names',
    23: 'Equality operator used with tuple variables',
    24: 'Assignment to function name',
    25: 'Assignment to tuple name',
    26: 'Assignment to tuple variable',
}

expected: dict[int, list[str]] = defaultdict(list)
exp_pattern = re.compile(r'^.*!!\s*((\d+,?)+)\|.*$')

with open('tests/typeErrors.base') as f:
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
