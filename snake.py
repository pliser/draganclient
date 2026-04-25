import tkinter as tk


CELL = 24
GRID_W = 22
GRID_H = 16
SPEED_MS = 120


class SnakeGame:
    def __init__(self, root):
        self.root = root
        self.root.title("Snake")
        self.root.resizable(False, False)

        self.canvas = tk.Canvas(
            root,
            width=GRID_W * CELL,
            height=GRID_H * CELL,
            bg="#10151a",
            highlightthickness=0,
        )
        self.canvas.pack()

        self.score_var = tk.StringVar()
        self.score_var.set("Score: 0")
        self.score_label = tk.Label(
            root, textvariable=self.score_var, font=("Consolas", 12)
        )
        self.score_label.pack(pady=(6, 0))

        self.seed = 1
        self.reset()
        self.bind_keys()
        self.running = True
        self.loop()

    def reset(self):
        self.snake = [(GRID_W // 2, GRID_H // 2)]
        self.direction = (1, 0)
        self.pending_dir = self.direction
        self.score = 0
        self.food = self.place_food()
        self.score_var.set("Score: 0")

    def bind_keys(self):
        self.root.bind("<Up>", lambda e: self.set_dir((0, -1)))
        self.root.bind("<Down>", lambda e: self.set_dir((0, 1)))
        self.root.bind("<Left>", lambda e: self.set_dir((-1, 0)))
        self.root.bind("<Right>", lambda e: self.set_dir((1, 0)))
        self.root.bind("w", lambda e: self.set_dir((0, -1)))
        self.root.bind("s", lambda e: self.set_dir((0, 1)))
        self.root.bind("a", lambda e: self.set_dir((-1, 0)))
        self.root.bind("d", lambda e: self.set_dir((1, 0)))

    def set_dir(self, nd):
        if (nd[0] != -self.direction[0]) or (nd[1] != -self.direction[1]):
            self.pending_dir = nd

    def place_food(self):
        while True:
            self.seed = (self.seed * 1103515245 + 12345) & 0x7fffffff
            x = self.seed % GRID_W
            self.seed = (self.seed * 1103515245 + 12345) & 0x7fffffff
            y = self.seed % GRID_H
            if (x, y) not in self.snake:
                return (x, y)

    def loop(self):
        if not self.running:
            return
        self.step()
        self.draw()
        self.root.after(SPEED_MS, self.loop)

    def step(self):
        self.direction = self.pending_dir
        head_x, head_y = self.snake[0]
        new_head = (head_x + self.direction[0], head_y + self.direction[1])

        if (
            new_head[0] < 0
            or new_head[0] >= GRID_W
            or new_head[1] < 0
            or new_head[1] >= GRID_H
            or new_head in self.snake
        ):
            self.game_over()
            return

        self.snake.insert(0, new_head)
        if new_head == self.food:
            self.score += 1
            self.score_var.set(f"Score: {self.score}")
            self.food = self.place_food()
        else:
            self.snake.pop()

    def draw(self):
        self.canvas.delete("all")
        self.draw_cell(self.food[0], self.food[1], "#ffcf5b")
        for i, (x, y) in enumerate(self.snake):
            color = "#46d5b2" if i == 0 else "#2fa389"
            self.draw_cell(x, y, color)

    def draw_cell(self, x, y, color):
        x1 = x * CELL
        y1 = y * CELL
        x2 = x1 + CELL
        y2 = y1 + CELL
        self.canvas.create_rectangle(x1, y1, x2, y2, fill=color, outline="#0b0f13")

    def game_over(self):
        self.running = False
        self.draw()
        self.canvas.create_rectangle(
            0,
            GRID_H * CELL // 2 - 32,
            GRID_W * CELL,
            GRID_H * CELL // 2 + 32,
            fill="#0b0f13",
            outline="",
        )
        self.canvas.create_text(
            GRID_W * CELL // 2,
            GRID_H * CELL // 2,
            text=f"Game Over  Score: {self.score}",
            fill="#e6f0ff",
            font=("Consolas", 16, "bold"),
        )


def main():
    root = tk.Tk()
    SnakeGame(root)
    root.mainloop()


if __name__ == "__main__":
    main()
