import random

ADJECTIVES = [
    "космическая",
    "кривая",
    "эпичная",
    "странная",
    "шумная",
    "ленивая",
    "киберпанковая",
    "мемная",
]

NOUNS = [
    "фигня",
    "штука",
    "схема",
    "история",
    "движуха",
    "магия",
    "затея",
    "конструкция",
]

VERBS = [
    "прыгает",
    "горит",
    "исчезает",
    "взрывается",
    "шипит",
    "танцует",
    "телепортируется",
    "искрит",
]

PLACES = [
    "в подвале",
    "на кухне",
    "в чате",
    "на орбите",
    "в лифте",
    "на балконе",
    "в гараже",
    "на сервере",
]


def generate_phrase() -> str:
    return (
        f"{random.choice(ADJECTIVES)} {random.choice(NOUNS)} "
        f"{random.choice(VERBS)} {random.choice(PLACES)}."
    )


def main() -> None:
    print("Генератор фигни:")
    for _ in range(5):
        print("-", generate_phrase())


if __name__ == "__main__":
    main()
