with open('nameErrors.errs') as f:
    lines = set(map(int, map(lambda x: x.split(':')[0], f.readlines())))

with open('nameErrors.base') as f:
    base = set(map(lambda x: x[0], filter(
        lambda x: ' !! ' in x[1], enumerate(f.readlines(), start=1))))

if (diff := base.intersection(lines)) != base:
    print('Some expected errors not found')
    print(f'{base.difference(lines) = }')
    print(f'{lines.difference(base) = }')
    import sys
    sys.exit(-1)

print('All expected errors found!')
