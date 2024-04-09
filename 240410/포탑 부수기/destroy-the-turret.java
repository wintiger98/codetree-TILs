import java.util.*;
import java.io.*;

public class Main {

    static class Pair {

        int x, y;

        public Pair(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Turret implements Comparable<Turret> {

        int x, y;
        int recent;
        int power;

        public Turret(int x, int y, int recent, int power) {
            this.x = x;
            this.y = y;
            this.recent = recent;
            this.power = power;
        }

        @Override
        public int compareTo(Turret o) {
            if (this.power != o.power) {
                return this.power - o.power;
            }
            if (this.recent != o.recent) {
                return o.recent - this.recent;
            }
            if (this.x + this.y != o.x + o.y) {
                return (o.x + o.y) - (this.x + this.y);
            }
            return o.y - this.y;
        }
    }

    final static int MAX_N = 10;
    static int[] dx = {0, 1, 0, -1};
    static int[] dy = {1, 0, -1, 0};
    static int[] dx2 = {0, 0, 0, -1, -1, -1, 1, 1, 1};
    static int[] dy2 = {0, -1, 1, 0, -1, 1, 0, -1, 1};

    static int N, M, K;
    static int turn;

    // 현재 포탑들이 가진 힘과 언제 공격했는지 기록
    static int[][] powerBoard = new int[MAX_N][MAX_N];
    static int[][] recordBoard = new int[MAX_N][MAX_N];

    // 레이저 공격 시 방문 여부와 경로 방향 기록
    static boolean[][] visited = new boolean[MAX_N][MAX_N];
    static int[][] backX = new int[MAX_N][MAX_N];
    static int[][] backY = new int[MAX_N][MAX_N];

    // 공격과 무관했는지 여부 저장
    static boolean[][] isActive = new boolean[MAX_N][MAX_N];

    static List<Turret> liveTurret = new ArrayList<>();

    // 턴을 진행하기 전 필요한 전처리
    static void init() {
        turn++;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                visited[i][j] = false;
                isActive[i][j] = false;
            }
        }
    }

    // 공격자 선정 -> 각성
    // 가장 약한 포탑이 N+M만큼 강해짐
    static void awake() {
        // 우선순위에 맞게 현재 살아있는 포탑 정렬
        Collections.sort(liveTurret);
        // 가장 약한 포탑을 찾아 N+M만큼 더해주기
        // isActive와 liveTurret 갱신
        Turret weakTurret = liveTurret.get(0);
        int x = weakTurret.x;
        int y = weakTurret.y;

        powerBoard[x][y] += N + M;
        recordBoard[x][y] = turn;
        weakTurret.power = powerBoard[x][y];
        weakTurret.recent = recordBoard[x][y];
        isActive[x][y] = true;

        liveTurret.set(0, weakTurret); // 업데이트
    }

    // 레이저 공격 진행
    static boolean laserAttack() {
        // 기존 정렬된 가장 앞선 포탑이 각성 포탑
        Turret weakTurret = liveTurret.get(0);
        int startX = weakTurret.x;
        int startY = weakTurret.y;
        int power = weakTurret.power;

        // 기존 정렬된 가장 뒤 포탑이 가장 강한 포탑 -> 타겟
        Turret strongTurret = liveTurret.get(liveTurret.size() - 1);
        int endX = strongTurret.x;
        int endY = strongTurret.y;

        // bfs를 통해 최단경로를 관리
        Queue<Pair> q = new ArrayDeque<>();
        visited[startX][startY] = true;
        q.add(new Pair(startX, startY));

        // 가장 쎈 포탑한테 도달 가능한지 여부 canAttack에 관리
        boolean canAttack = false;

        while (!q.isEmpty()) {
            Pair pair = q.poll();

            // 가장 강한 포탑한테 도달할 수 있다면, 바로 멈춤
            if (pair.x == endX && pair.y == endY) {
                canAttack = true;
                break;
            }

            // 각각 우 -> 하 -> 좌 -> 상 순서대로 방문해서 방문 가능 포탑 찾고 큐에 저장
            for (int i = 0; i < 4; i++) {
                int cx = (pair.x + dx[i] + N) % N;
                int cy = (pair.y + dy[i] + M) % M;

                // 이미 방문한거면 넘기기
                if (visited[cx][cy]) {
                    continue;
                }

                // 벽이면 넘어가기
                if (powerBoard[cx][cy] == 0) {
                    continue;
                }

                visited[cx][cy] = true;
                backX[cx][cy] = pair.x;
                backY[cx][cy] = pair.y;
                q.add(new Pair(cx, cy));
            }
        }
        // 도달 가능하다면 공격 진행
        if (canAttack) {
            // 가장 강한 포탑은 power만큼 공격
            powerBoard[endX][endY] -= power;
            powerBoard[endX][endY] = Math.max(0, powerBoard[endX][endY]);
            isActive[endX][endY] = true;

            // 기존 경로 역추적
            // 경로 상 모든 포탑에게 power / 2만큼 공격 진행
            int cx = backX[endX][endY];
            int cy = backY[endX][endY];

            while (!(cx == startX && cy == startY)) {
                powerBoard[cx][cy] -= power / 2;
                powerBoard[cx][cy] = Math.max(0, powerBoard[cx][cy]);
                isActive[cx][cy] = true;

                int nextCx = backX[cx][cy];
                int nextCy = backY[cx][cy];

                cx = nextCx;
                cy = nextCy;
            }
        }
        return canAttack;
    }

    // 레이저 공격을 못하면 폭탄 공격 진행
    static void bombAttack() {
        // 기존 정렬된 가장 앞선 포탑이 각성 포탑
        Turret weakTurret = liveTurret.get(0);
        int startX = weakTurret.x;
        int startY = weakTurret.y;
        int power = weakTurret.power;

        // 기존 정렬된 가장 뒤 포탑이 가장 강한 포탑 -> 타겟
        Turret strongTurret = liveTurret.get(liveTurret.size() - 1);
        int endX = strongTurret.x;
        int endY = strongTurret.y;

        for (int i = 0; i < 9; i++) {
            int cx = (endX + dx2[i] + N) % N;
            int cy = (endY + dy2[i] + M) % M;

            // 각성 포탑일경우 넘어가기
            if (cx == startX && cy == startY) {
                continue;
            }

            // 가장 강한 포탑이면 power만큼 공격
            if (cx == endX && cy == endY) {
                powerBoard[cx][cy] -= power;
            } else {
                powerBoard[cx][cy] -= power / 2;
            }
            // 그 외의 경우 pow / 2만큼 공격
            powerBoard[cx][cy] = Math.max(0, powerBoard[cx][cy]);
            isActive[cx][cy] = true;
        }
    }

    static void reserve() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                if (isActive[i][j]) {
                    continue;
                }
                if (powerBoard[i][j] == 0) {
                    continue;
                }
                powerBoard[i][j]++;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        K = Integer.parseInt(st.nextToken());

        for (int i = 0; i < N; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < M; j++) {
                powerBoard[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        // k턴 동안 진행
        while (K-- > 0) {
            // 턴 진행 전 살아있는 포탑 정리
            liveTurret = new ArrayList<>();
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < M; j++) {
                    if (powerBoard[i][j] > 0) {
                        liveTurret.add(new Turret(i, j, recordBoard[i][j], powerBoard[i][j]));
                    }
                }
            }

            // 살아있는 포탑이 1개 이하면 바로 종료
            if (liveTurret.size() <= 1) {
                break;
            }

            // 턴 진행 전 초기화
            init();

            // 각성
            awake();

            // 레이저 or 포격
            boolean isSuccess = laserAttack();
            if (!isSuccess) {
                bombAttack();
            }

            // 공격 관여 X -> 1씩 힘 증가
            reserve();
        }

        // 살아있는 포탑 중 가장 큰 힘
        int ans = 0;
        for(int i = 0; i < N; i++)
            for(int j = 0; j < M; j++)
                ans = Math.max(ans, powerBoard[i][j]);

        System.out.print(ans);
    }
}