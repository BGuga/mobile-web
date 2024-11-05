import os
import socket
from datetime import datetime

class SocketServer:
    def __init__(self):
        self.buf_size = 4096  # 버퍼 크기 설정
        try:
            with open('./response.bin', 'rb') as file:
                self.RESPONSE = file.read()  # 응답 파일 읽기
        except FileNotFoundError:
            self.RESPONSE = b"HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nDefault response"
            print("response.bin not found. Using default response message.")

        self.DIR_PATH = './request'
        self.createDir(self.DIR_PATH)

    def createDir(self, path):
        """디렉토리 생성"""
        try:
            if not os.path.exists(path):
                os.makedirs(path)
        except OSError as e:
            print(f"Error: Failed to create the directory. {e}")

    def saveRequestData(self, data):
        """클라이언트 요청 데이터를 파일로 저장"""
        timestamp = datetime.now().strftime("%Y-%m-%d-%H-%M-%S")
        file_path = os.path.join(self.DIR_PATH, f"{timestamp}.bin")
        try:
            with open(file_path, 'wb') as file:
                file.write(data)
            print(f"Request data saved to {file_path}")
        except Exception as e:
            print(f"Error: {e}")

    def run(self, ip, port):
        """서버 실행"""
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.sock.bind((ip, port))
        self.sock.listen(10)
        print("Start the socket server...")
        print("\\\"Ctrl+C\\\" for stopping the server\n")

        try:
            while True:
                clnt_sock, req_addr = self.sock.accept()
                clnt_sock.settimeout(20.0)  # 타임아웃 시간을 20초로 설정
                print(f"Request from {req_addr}")

                # 데이터 수신
                request_data = b""
                try:
                    while True:
                        chunk = clnt_sock.recv(self.buf_size)
                        if not chunk:
                            break
                        request_data += chunk
                except socket.timeout:
                    print("Socket timed out while receiving data.")

                # 요청 데이터 저장
                self.saveRequestData(request_data)

                # 응답 전송
                clnt_sock.sendall(self.RESPONSE)
                print("Response sent to client.")
                
                # 요청 데이터 출력
                print("Request received:")
                print(request_data.decode('utf-8', errors='ignore'))  # 요청 내용을 터미널에 출력
                
                # 클라이언트 소켓 닫기
                clnt_sock.close()
        except KeyboardInterrupt:
            print("\n\nStop the server...")

        # 서버 소켓 닫기
        self.sock.close()

if __name__ == "__main__":
    server = SocketServer()
    server.run("127.0.0.1", 8000)
