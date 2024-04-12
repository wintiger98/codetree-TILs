import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Main {
    final static int MAX_N = 20;
    static int[] dx = {-1, 0, 1, 0}; // 상 우 하 좌
    static int[] dy = {0, 1, 0, -1};
    static int N, M;
    static ArrayList<Player>[][] playerBoard = new ArrayList[MAX_N][MAX_N];
    static ArrayList<Integer>[][] guns = new ArrayList[MAX_N][MAX_N];
    static Player[] players;
    static int[] points;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        int K = Integer.parseInt(st.nextToken());

        players = new Player[M];
        points = new int[M];

        // 총 입력 , playerBoard 초기화
        for (int i = 0; i < N; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < N; j++) {
                guns[i][j] = new ArrayList<>();
                playerBoard[i][j] = new ArrayList<>();
                int gun = Integer.parseInt(st.nextToken());
                if (gun > 0)
                    guns[i][j].add(gun);
            }
        }

        // 플레이어 입력
        for (int i = 0; i < M; i++) {
            st = new StringTokenizer(br.readLine());
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
            int d = Integer.parseInt(st.nextToken());
            int s = Integer.parseInt(st.nextToken());
            x--;
            y--;
            players[i] = new Player(i, x, y, d, s);
            playerBoard[x][y].add(players[i]);
        }


        while (K-- > 0) {
            for (Player player : players) {
                // 1. 플레이어 이동
                player.move();
                // 2. 플레이어가 있는지 확인
                // 2.1 만약 플레이어가 없으면, 총 확인 -> 획득
                if (!player.isTherePlayer()) {
                    player.checkAndGetGun();
                }
                // 2.2 플레이어가 있다면, fight
                else {
                    fight(player);
                }
//                printGuns();
//                printPlayers();
            }
        }

        // 정답
        for (int point : points) {
            bw.write(point + " ");
        }
        bw.close();
    }

    static void printPlayers() {
        System.out.println("플레이어!!");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                System.out.print(playerBoard[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }

    static void printGuns() {
        System.out.println("총총!!");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                System.out.print(guns[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }

    static void fight(Player player1) {
        Player player2 = playerBoard[player1.x][player1.y].get(0);
        Player winner = null;
        Player loser = null;
        // 총 합이 더 큰사람이 위너
        if (player1.getTotalPower() > player2.getTotalPower()) {
            winner = player1;
            loser = player2;
        } else if (player1.getTotalPower() < player2.getTotalPower()) {
            winner = player2;
            loser = player1;
        } else {
            // 같으면, 기본 능력치로 판단
            if (player1.strong > player2.strong) {
                winner = player1;
                loser = player2;
            } else {
                winner = player2;
                loser = player1;
            }
        }
        // 승자 : 포인트 획득
        points[winner.idx] += winner.getTotalPower() - loser.getTotalPower();

        // 패자 : 총 격자에 내려놓기
        if (loser.gun > 0) {
            guns[loser.x][loser.y].add(loser.gun); // 격자에 총 추가
            loser.gun = 0; // 총 삭제
        }
        int cx = loser.x, cy = loser.y;
        // 패자 : 원래 방향대로 한 칸 이동, 사람 있으면 90도 씩 회전
        for (int i = 0, dir = loser.dir; i < 4; i++, dir = (dir + 1) % 4) {
            cx = loser.x + dx[dir];
            cy = loser.y + dy[dir];
            // 격자 범위 밖이면 회전
            if (cx < 0 || cx >= N || cy < 0 || cy >= N) continue;
            // 플레이어가 있으면 회전
            if (!playerBoard[cx][cy].isEmpty()) continue;
            break;
        }
        // 원래 위치 없애기
        playerBoard[loser.x][loser.y].remove(loser);
        // 빈칸으로 이동
        loser.x = cx;
        loser.y = cy;
        // 새로운 위치에 추가
        playerBoard[cx][cy].add(loser);
        // 총획득
        loser.checkAndGetGun();

        // 승자는 위치에 있는 총들중 가장 쎈 총 획득
        winner.checkAndGetGun();
    }

    static class Player {
        int idx;
        int x, y;
        int dir, strong;
        Integer gun;

        @Override
        public String toString() {
            return "Player{" +
                    idx +
                    ", " + gun +
                    '}';
        }

        public int getTotalPower() {
            return gun + strong;
        }

        public Player(int idx, int x, int y, int dir, int strong) {
            this.idx = idx;
            this.x = x;
            this.y = y;
            this.dir = dir;
            this.strong = strong;
            this.gun = 0;
        }

        public void move() { // 방향대로 이동
            int cx = x + dx[dir];
            int cy = y + dy[dir];
            // 범위 벗어나면, 방향 정반대로 가기 (0<->2, 1<->3)
            if (cx < 0 || cx >= N || cy < 0 || cy >= N) {
                cx = x - dx[dir];
                cy = y - dy[dir];
                this.dir = (this.dir + 2) % 4;
            }

            playerBoard[cx][cy].add(this);
            playerBoard[x][y].remove(this);

            this.x = cx;
            this.y = cy;
        }

        public boolean isTherePlayer() {
            // 두명 이상 있으면 플레이어 존재한다는것!
            return playerBoard[this.x][this.y].size() > 1;
        }

        public void checkAndGetGun() {
            // 총이 있다면, 가장 센 총 획득하기
            if (!guns[x][y].isEmpty()) {
                int maxIdx = -1;
                int maxValue = this.gun;
                for (int i = 0; i < guns[x][y].size(); i++) {
                    if (maxValue < guns[x][y].get(i)) {
                        maxValue = guns[x][y].get(i);
                        maxIdx = i;
                    }
                }
                if (maxIdx != -1) {
                    int originGun = this.gun;
                    // 선택
                    this.gun = guns[x][y].get(maxIdx);
                    // 삭제
                    guns[x][y].remove(maxIdx);
                    guns[x][y].add(originGun);
                }
            }
        }
    }
}