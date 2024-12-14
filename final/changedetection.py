import os
import cv2
import pathlib
import requests
from datetime import datetime
from decideDogAction import DecideDogAction

class ChangeDetection:
    result_prev = []
    HOST = 'http://127.0.0.1:8000/'
    username = 'admin'
    password = 'east2424'
    token = "4bdce80c35b857798f5e37222181105c9e016bd9"
    title = "title"
    text = "text"
    current_dog_status = "None"

    def __init__(self, names):
        self.result_prev = [0 for i in range(len(names))]

        res = requests.post(self.HOST + '/api-token-auth/', {
            'username': self.username,
            'password': self.password,
        })

        res.raise_for_status()
        self.decide = DecideDogAction()
        self.token = res.json()['token']  # 토큰 저장

    def add(self, names, detected_current, save_dir, image, detections_with_coords):
        self.title = ""
        self.text = ""
        change_flag = 0  # 변화 감지 플래그
        i = 0
        while i < len(self.result_prev):
            if i == 16 and detected_current[i] == 1:
                dog_status = self.decide.update(detections_with_coords)
                print(dog_status)
                if dog_status != self.current_dog_status:
                    change_flag = 1
                    self.current_dog_status = dog_status
                # change_flag = 1
                self.title = names[i]
                self.text += names[i] + " is " + self.current_dog_status

                
            i += 1

        self.result_prev = detected_current[:]  # 객체 검출 상태 저장

        if change_flag == 1:
            self.send(save_dir, image)
    
    def send(self, save_dir, image):
        now = datetime.now()
        now.isoformat()

        today = datetime.now()
        save_path = os.getcwd() / save_dir / 'detected' / str(today.year) / str(today.month) / str(today.day)
        pathlib.Path(save_path).mkdir(parents=True, exist_ok=True)

        full_path = save_path / '{0}-{1}-{2}-{3}.jpg'.format(today.hour, today.minute, today.second, today.microsecond)

        dst = cv2.resize(image, dsize=(320, 240), interpolation=cv2.INTER_AREA)
        cv2.imwrite(full_path, dst)

        # 인증이 필요한 요청에 아래의 headers를 붙임
        headers = {'Authorization': 'JWT ' + self.token, 'Accept': 'application/json'}

        # Post Create
        data = {
            'author' : "1",
            'title': self.title,
            'text': self.text,
            'created_date': now,
            'published_date': now
        }

        file = {'image': open(full_path, 'rb')}

        res = requests.post(self.HOST + '/api_root/post/', data=data, files=file, headers=headers)
        print(res)
    

    