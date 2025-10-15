from selenium import webdriver
from selenium.webdriver.common.by import By
import chromedriver_autoinstaller
import json
import time
import os

# Auto-install ChromeDriver
chromedriver_autoinstaller.install()

# Base URL
BASE_URL = "https://www.playqueensgame.com/puzzles/{size}x{size}/{level}"

# Define board sizes and number of levels
board_levels = {
    7: 50,
    8: 130,
    9: 110,
    10: 60,
    11: 50
}

# Create output folder
os.makedirs("boards", exist_ok=True)

# Launch browser
driver = webdriver.Chrome()

for size, max_level in board_levels.items():
    for level in range(1, max_level + 1):
        url = BASE_URL.format(size=size, level=level)
        driver.get(url)
        time.sleep(2)  # allow page to load

        # Extract cells
        cells = driver.find_elements(By.CSS_SELECTOR, "div[data-row][data-col]")
        board = []
        for cell in cells:
            row = int(cell.get_attribute("data-row"))
            col = int(cell.get_attribute("data-col"))
            color = cell.value_of_css_property("background-color")
            board.append({
                "row": row,
                "col": col,
                "color": color
            })

        # Sort for consistency
        board.sort(key=lambda c: (c["row"], c["col"]))

        # Save JSON
        filename = f"boards/{size}x{size}_level{level}.json"
        with open(filename, "w") as f:
            json.dump(board, f, indent=2)

        print(f"✅ Saved {filename}")

driver.quit()
print("✅ All boards extracted!")
