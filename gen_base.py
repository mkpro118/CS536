'''--------------------------------------------------------------------------------
||||                               USER MANUAL                              ||||
--------------------------------------------------------------------------------

Use the following functions to generate code.

--------------------------------------------------------------------------------
generate_post_incr_stmt(tuple_: bool = False)

--> tuple_ = True  will use a tuple member instead of a regular identifier
--------------------------------------------------------------------------------


--------------------------------------------------------------------------------
generate_post_decr_stmt(tuple_: bool = False)

--> tuple_ = True  will use a tuple member instead of a regular identifier
--------------------------------------------------------------------------------


--------------------------------------------------------------------------------
generate_if_stmt(simple: bool = True, decls: bool | int = True,
                 stmts: bool | int = True, ret: bool = True, retval: bool = True)

--> simple = False  will generate a complex condition value
--> Specifying any other param as false will result in that section not being generated
--> retval is only considered if ret = True
--> if decls or stmts are positive ints, that many decls/stmts will be generated
--------------------------------------------------------------------------------


--------------------------------------------------------------------------------
generate_if_else_stmt(simple: bool = True, decls1: bool = True,
                      stmts1: bool = True, ret1: bool = True, retval1: bool = True,
                      decls2: bool = True, stmts2: bool = True, ret2: bool = True,
                      retval2: bool = True)

--> same type of args as generate_if_stmt
--> postfix "1" refers to the body of the if block
--> postfix "2" refers to the body of the else block
--------------------------------------------------------------------------------


--------------------------------------------------------------------------------
generate_while_stmt(simple: bool = True, decls: bool = True, stmts: bool = True,
                    ret: bool = True, retval: bool = True)

--> simple = False  will generate a complex condition value
--> Specifying any other param as false will result in that section not being generated
--> retval is only considered if ret = True
--> if decls or stmts are positive ints, that many decls/stmts will be generated
--------------------------------------------------------------------------------


--------------------------------------------------------------------------------
generate_read_stmt(tuple_: bool = False)

--> tuple_ = True  will use a tuple member instead of a regular identifier
--------------------------------------------------------------------------------


--------------------------------------------------------------------------------
generate_write_stmt(tuple_: bool = False)

--> tuple_ = True  will use a tuple member instead of a regular identifier
--------------------------------------------------------------------------------


--------------------------------------------------------------------------------
generate_func_call_stmt(args: int = 0)

--> args specifies how many arguments the function call takes
--------------------------------------------------------------------------------


--------------------------------------------------------------------------------
generate_assgn_stmt(tuple_lhs: bool = False, simple: bool = True, depth: int = 2)

--> tuple_lhs = True  will assign to a tuple member instead of a regular identifier
--> simple = False  will generate a complicated mathematical rhs value
--> depth is only considered if simple = False
--> depth indicated the exponential complexity of the rhs, i.e. depth = n will
    generate 2 ** n terms in the rhs. however, float values are equivalent to their
    ceil value.
--------------------------------------------------------------------------------


--------------------------------------------------------------------------------
generate_func_stmt(args: bool = True, decls: bool = True, stmts: bool = True,
                   ret: bool = True, retval: bool = True)

--> specifying any param as false will omit that section from the generated function
--------------------------------------------------------------------------------


--------------------------------------------------------------------------------
func_wrap(stmt: str)

--> wraps any statement in a function body, since base doesn't support top level
--> statements.
--------------------------------------------------------------------------------
'''

import random
from string import ascii_letters as letters, digits
import itertools
import collections
import functools

operators = {
    'Plus': '+',
    'Minus': '-',
    'Times': '*',
    'Divide': '/',
    'Equals': '==',
    'Greater': '>',
    'GreaterEq': '>=',
    'Lesser': '<',
    'LesserEq': '<=',
    'NotEquals': '~=',
}

math_ops = tuple('+-*/')
rel_ops = ('==', '>', '>=', '<', '<=', '~=')
logic_ops = tuple('&|')

data_types = ('integer', 'logical', 'void')
whitespaces = '\n '
id_charset = letters + digits + '_'
char_1_charset = letters + '_'
op_types = (
    ('Integer', lambda: f'{random.randint(0, 10)}'),
    ('Identifier', lambda: rand_id),
    ('Tuple Member', lambda: rand_tuple(2, 1)),
)

rand_int = random.randint
exhaust = functools.partial(collections.deque, maxlen=0)
rand_type = functools.partial(random.choice, data_types)
whitespace = functools.partial(random.choice, whitespaces)
rand_range = functools.partial(lambda _: range(rand_int(1, 5)), None)
rand_bool = functools.partial(random.choice, [True, False])
randint_ = functools.partial(rand_int, 1, 10)


def rand_id(n=5):
    s = random.choice(char_1_charset)
    if n == 1:
        return s
    return s + ''.join(random.choices(id_charset, k=(n - 1)))


def rand_tuple(tuple_len=5, member_len=5):
    return f'{rand_id(tuple_len)}:{rand_id(member_len)}'


def rand_lit():
    if (n := random.random()) < 0.25:
        return f'{rand_int(-2 ** 31, 2 ** 31)}'
    elif n < 0.5:
        return 'True'
    elif n < 0.75:
        return 'False'
    else:
        return f'"{rand_id(10)}"'


rhs_terms = [rand_tuple, rand_id, randint_]


def rhs_term():
    return random.choice(rhs_terms)()


def logical_terms():
    return random.choice(['True', 'False',
                          f'{rhs_term()} {random.choice(rel_ops)} {rhs_term()}'])


def rand_exp(exp_gen, ops, depth):
    if depth <= 0:
        return f'{exp_gen()}'

    left = rand_exp(exp_gen, ops, depth - 1)
    right = rand_exp(exp_gen, ops, depth - 1)
    if random.random() > 0.8:
        left = f'({left})'
    if random.random() > 0.8:
        right = f'({right})'

    op = random.choice(ops)
    ret = f'{left} {op} {right}'
    if random.random() > 0.8:
        ret = f'({ret})'

    return ret


rand_math_exp = functools.partial(rand_exp, rhs_term, math_ops)
rand_logical_exp = functools.partial(rand_exp,
                                     logical_terms,
                                     logic_ops)


def rand_decls(n_decls):
    if len(n_decls) <= 0:
        return ''
    return '.'.join(map(lambda _: f'{whitespace()}{rand_type()} {rand_id(3)}',
                        n_decls)) + '.'


def generate_random_statment():
    if (n := rand_int(1, 9)) == 1:
        return generate_post_incr_stmt(rand_bool())
    elif n == 2:
        return generate_post_decr_stmt(rand_bool())
    elif n == 3:
        return generate_if_stmt(*(rand_bool() for _ in range(5)))
    elif n == 4:
        return generate_if_else_stmt(*(rand_bool() for _ in range(9)))
    elif n == 5:
        return generate_while_stmt(*(rand_bool() for _ in range(5)))
    elif n == 6:
        return generate_read_stmt(rand_bool())
    elif n == 7:
        return generate_write_stmt(rand_bool())
    elif n == 8:
        return generate_func_call_stmt(rand_int(0, 4))
    else:
        return generate_assgn_stmt(*(rand_bool() for _ in range(2)), rand_int(0, 2))


def rand_stmts(n_stmts):
    return '\n'.join(generate_random_statment() for _ in n_stmts)


def rand_ret(retval=True):
    if retval:
        if (n := random.random()) < 0.3:
            retval_ = rand_range()
        elif n < 0.6:
            retval_ = rand_id(4)
        else:
            retval_ = random.choice([True, False])
        return f'return {retval_}.'
    else:
        return 'return.'


def generate_op_tests():
    for operator, sign in operators.items():
        for lhs, rhs in itertools.product(op_types, op_types):
            yield (
                f'\n!! {operator} operator, LHS: {lhs[0]}, RHS: {rhs[0]}'
                f'void test_func {{}} [_ = {lhs[1]()} {sign} {rhs[1]()}.]'
            )


def generate_postfix_test(op, tuple_=False):
    return f'{rand_tuple() if tuple_ else rand_id()}{op}.'


generate_post_incr_stmt = functools.partial(generate_postfix_test, '++')
generate_post_decr_stmt = functools.partial(generate_postfix_test, '--')


def generate_block(keyword, cond, simple=True, decls=True, stmts=True, ret=True, retval=True):
    cond_ = ''
    if cond:
        cond_ = rand_logical_exp(0 if simple else rand_int(1, 3))

    decls_ = ''
    if decls:
        n_decls = range(decls) if isinstance(decls, int) else rand_range()
        decls_ = rand_decls(n_decls)

    stmts_ = ''
    if stmts:
        n_stmts = range(stmts) if isinstance(stmts, int) else rand_range()
        decls_ = rand_stmts(n_stmts)

    ret_ = ''
    if ret:
        ret_ = rand_ret(retval)

    return (
        f'{keyword} {cond_}{whitespace()}[{whitespace()}'
        f'{decls_}{whitespace()}{stmts_}{whitespace()}{ret_}{whitespace()}]'
        f'{whitespace()}'
    )


generate_if_stmt = functools.partial(generate_block, 'if', True)
generate_while_stmt = functools.partial(generate_block, 'while', True)
generate_else_stmt = functools.partial(generate_block, 'else', False)


def generate_if_else_stmt(simple=True, decls1=True, stmts1=True, ret1=False, retval1=False,
                          decls2=True, stmts2=True, ret2=False, retval2=False):
    if_ = generate_if_stmt(simple=simple, decls=decls1,
                           stmts=stmts1, ret=ret1, retval=retval1)
    else_ = generate_else_stmt(decls=decls2, stmts=stmts2,
                               ret=ret2, retval=retval2)
    return f'{if_}{whitespace()}{else_}'


def generate_io_test(op, tuple_=False):
    return f'{op} {(rand_tuple if tuple_ else rand_id)()}.'


generate_read_stmt = functools.partial(generate_io_test, 'read >>')
generate_write_stmt = functools.partial(generate_io_test, 'write <<')


def generate_func_call_stmt(args=0):
    return f'{rand_id()}({", ".join([(rand_id if random.random() > 0.5 else rand_lit)() for _ in range(args)])}).'


def generate_assgn_stmt(tuple_lhs=False, simple=True, depth=2):
    if tuple_lhs:
        lhs = rand_tuple()
    else:
        lhs = rand_id()

    if simple:
        if random.random() > 0.5:
            op = random.choice(tuple(operators.values()))
            rhs = f'{rhs_term()} {op} {rhs_term()}'
        else:
            rhs = f'{rhs_term()}'
    else:
        rhs = rand_math_exp(depth)

    return f'{lhs} = {rhs}.'


def generate_func_stmt(args=True, decls=True, stmts=True, ret=True, retval=True):
    args_ = ''
    if args:
        args_ = ', '.join(map(lambda _: f'{rand_type()} {rand_id(3)}',
                              rand_range()))
    decls_ = ''
    if decls:
        n_decls = range(decls) if isinstance(decls, int) else rand_range()
        decls_ = rand_decls(n_decls)

    stmts_ = ''
    if stmts:
        n_stmts = range(stmts or 1) if isinstance(stmts, int) else rand_range()
        stmts_ = rand_stmts(n_stmts)

    ret_ = ''
    if ret:
        ret_ = rand_ret(retval)

    return (
        f'{rand_type()} {rand_id(5)} {{{args_}}} [{whitespace()}'
        f'{decls_}{whitespace()}'
        f'{stmts_}{whitespace()}'
        f'{ret_}\n]'
    )


def func_wrap(stmt):
    return (
        f'{rand_type()} {rand_id(5)} {{}} [{whitespace()}{stmt}\n]'
    )


if __name__ == '__main__':
    # exhaust(map(print, generate_op_tests()))
    args = tuple(rand_bool() for _ in range(5))
    print(f'using {args = }')
    print(func_wrap(generate_if_stmt(*args)))
