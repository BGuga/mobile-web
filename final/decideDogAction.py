import time
from collections import deque
from math import sqrt

class DecideDogAction:
    def __init__(self, proximity_threshold=50, activity_threshold=100, sleep_threshold=200):
        """
        Initializes the DecideDogAction instance.
        :param proximity_threshold: 거리 임계값 (두 객체가 가까운 것으로 간주되는 거리, 픽셀 단위).
        :param activity_threshold: 활동 상태를 판단하는 시간 (초).
        :param sleep_threshold: 수면 상태를 판단하는 시간 (초).
        """
        self.proximity_threshold = proximity_threshold
        self.activity_threshold = activity_threshold
        self.sleep_threshold = sleep_threshold

        # 위치 기록
        self.dog_positions = deque(maxlen=sleep_threshold)  # 최근 1분 간 강아지 위치 저장
        self.bowl_positions = "None"
        self.last_action_time = time.time()  # 마지막 상태 변화 시간
        self.current_state = "Unknown"  # 현재 상태 ("Eating", "Playing", "Sleeping")

    def calculate_distance(self, pos1, pos2):
        """
        Calculates the Euclidean distance between two points.
        :param pos1: (x1, y1) 좌표.
        :param pos2: (x2, y2) 좌표.
        :return: 두 좌표 간 거리.
        """
        return sqrt((pos1[0] - pos2[0]) ** 2 + (pos1[1] - pos2[1]) ** 2)

    def update(self, detections_with_coords):
        """
        Updates the position and determines the dog's activity.
        :param detections_with_coords: 강아지와 그릇의 위치 정보가 포함된 리스트.
        """
        dog_position = None
        bowl_position = None

        # 강아지와 그릇 위치 추출
        for detection in detections_with_coords:
            if detection["label"] == "dog":
                bbox = detection["bbox"]
                dog_position = ((bbox[0] + bbox[2]) // 2, (bbox[1] + bbox[3]) // 2)  # 중심 좌표
            elif detection["label"] == "bowl":
                bbox = detection["bbox"]
                bowl_position = ((bbox[0] + bbox[2]) // 2, (bbox[1] + bbox[3]) // 2)  # 중심 좌표

        # 강아지 위치 기록
        if dog_position:
            self.dog_positions.append(dog_position)

        # 그릇 위치 기록
        if bowl_position:
            self.bowl_positions = bowl_position

        # 밥 먹는 상태 판단
        if dog_position and bowl_position:
            bowl_distance = self.calculate_distance(self.bowl_positions, dog_position)
            if  bowl_distance< 450:
                self.set_state("Eating")
                return self.current_state

        # 놀고 있는 상태 판단
        if len(self.dog_positions) == self.sleep_threshold:
            first_pos = self.dog_positions[0]
            last_pos = self.dog_positions[-1]
            distance = self.calculate_distance(first_pos, last_pos)
            # 첫 번째 위치와 마지막 위치 사이의 거리가 proximity_threshold보다 크면 "Playing" 상태로 판단
            if distance > self.proximity_threshold:
                self.set_state("Playing")
                return self.current_state
            if all(
                self.calculate_distance(self.dog_positions[0], pos) < self.proximity_threshold
                for pos in self.dog_positions
            ):  # 1분 동안 가만히 있음
                self.set_state("Sleeping")
                return self.current_state

        return self.current_state

    def set_state(self, new_state):
        """
        Sets a new state for the dog if it changes.
        :param new_state: 새로운 상태 ("Eating", "Playing", "Sleeping").
        """
        if self.current_state != new_state:
            self.current_state = new_state
            self.last_action_time = time.time()
            print(f"Dog is now {new_state}.")
