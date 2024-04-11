import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Main {

    static class Pair {

        int x, y;

        public Pair(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public boolean isSame(Pair p) {
            return this.x == p.x && this.y == p.y;
        }
    }

    static final int MAX_N = 100;
    static int N, M, H; // 격자 크기, 도망자 명수, 나무 그루수, 시간

    // 각 칸의 도망자 정보 관리(방향만 저장)
    static List<Integer>[][] hiders = new ArrayList[MAX_N][MAX_N];
    static List<Integer>[][] nextHiders = new ArrayList[MAX_N][MAX_N];
    public static boolean[][] tree = new boolean[MAX_N][MAX_N];

    // 정방향 기준으로 현재 위치에서 술래가 움직여야할 방향 관리
    static int[][] seekerNextDir = new int[MAX_N][MAX_N];
    // 역방향 기준 술래 움직일 방향 관리
    static int[][] seekerRevDir = new int[MAX_N][MAX_N];

    // 술래 현재 위치
    static Pair seekerPos;
    // 술래가 움직이는 방향이 정방향인지 여부
    static boolean forwardFacing = true;

    static int ans;

    // 정중앙으로부터 끝까지 움직이는 경로를 계산해줍니다.
    public static void initSeekerPath() {
        // 상우하좌 순서대로 넣어줍니다.
        int[] dx = new int[]{-1, 0, 1,  0};
        int[] dy = new int[]{0 , 1, 0, -1};

        // 시작 위치와 방향,
        // 해당 방향으로 이동할 횟수를 설정합니다.
        int currX = N / 2, currY = N / 2;
        int moveDir = 0, moveNum = 1;

        while(currX > 0 || currY > 0) {
            // moveNum 만큼 이동합니다.
            for(int i = 0; i < moveNum; i++) {
                seekerNextDir[currX][currY] = moveDir;
                currX += dx[moveDir]; currY += dy[moveDir];
                seekerRevDir[currX][currY] = (moveDir < 2) ? (moveDir + 2) : (moveDir - 2);

                // 이동하는 도중 (0, 0)으로 오게 되면,
                // 움직이는 것을 종료합니다.
                if(currX == 0 && currY == 0)
                    break;
            }

            // 방향을 바꿉니다.
            moveDir = (moveDir + 1) % 4;
            // 만약 현재 방향이 위 혹은 아래가 된 경우에는
            // 특정 방향으로 움직여야 할 횟수를 1 증가시킵니다.
            if(moveDir == 0 || moveDir == 2)
                moveNum++;
        }
    }

    // 격자 내에 있는지를 판단합니다.
    public static boolean inRange(int x, int y) {
        return 0 <= x && x < N && 0 <= y && y < N;
    }

    public static void hiderMove(int x, int y, int moveDir) {
        // 좌우하상 순서대로 넣어줍니다.
        int[] dx = new int[]{0 , 0, 1, -1};
        int[] dy = new int[]{-1, 1, 0,  0};

        int nx = x + dx[moveDir], ny = y + dy[moveDir];
        // Step 1.
        // 만약 격자를 벗어난다면
        // 우선 방향을 틀어줍니다.
        if(!inRange(nx, ny)) {
            // 0 <-> 1 , 2 <-> 3이 되어야 합니다.
            moveDir = (moveDir < 2) ? (1 - moveDir) : (5 - moveDir);
            nx = x + dx[moveDir]; ny = y + dy[moveDir];
        }

        // Step 2.
        // 그 다음 위치에 술래가 없다면 움직여줍니다.
        if(!new Pair(nx, ny).isSame(seekerPos))
            nextHiders[nx][ny].add(moveDir);
            // 술래가 있다면 더 움직이지 않습니다.
        else
            nextHiders[x][y].add(moveDir);
    }

    // 현재 술래 위치 불러오기
    public static int distFromSeeker(int x, int y) {
        // 현재 술래의 위치를 불러옵니다.
        int seekerX = seekerPos.x;
        int seekerY = seekerPos.y;

        return Math.abs(seekerX - x) + Math.abs(seekerY - y);
    }

    public static void hiderMoveAll() {
        // 1. next hider 초기화
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                nextHiders[i][j] = new ArrayList<>();
            }
        }

        // 2. hider 움직이기
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                // 술래와 거리가 3 이내인 도망자들에 대해 움직이기
                if (distFromSeeker(i, j) <= 3) {
                    for (int k = 0; k < hiders[i][j].size(); k++) {
                        hiderMove(i, j, hiders[i][j].get(k));
                    }
                } else { // 그렇지 않으면 현재 위치에 넣기
                    for (int k = 0; k < hiders[i][j].size(); k++) {
                        nextHiders[i][j].add(hiders[i][j].get(k));
                    }
                }
            }
        }

        // 3. next hider 값을 옮겨주기
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                hiders[i][j] = new ArrayList<>(nextHiders[i][j]);
            }
        }
    }

    // 현재 술래가 바라보는 방향을 가져옵니다.
    public static int getSeekerDir() {
        // 현재 술래의 위치를 불러옵니다.
        int x = seekerPos.x;
        int y = seekerPos.y;

        // 어느 방향으로 움직여야 하는지에 대한 정보를 가져옵니다.
        int moveDir;
        if(forwardFacing)
            moveDir = seekerNextDir[x][y];
        else
            moveDir = seekerRevDir[x][y];

        return moveDir;
    }

    public static void checkFacing() {
        // 1. 정방향으로 끝에 다다른 경우, 방향 바꾸기
        if (seekerPos.isSame(new Pair(0, 0)) && forwardFacing) {
            forwardFacing = false;
        }
        // 2. 역방향으로도 끝에 다다른 경우, 방향 바꾸기
        if (seekerPos.isSame(new Pair(N / 2, N / 2)) && !forwardFacing) {
            forwardFacing = true;
        }
    }

    public static void seekerMove() {
        int x = seekerPos.x;
        int y = seekerPos.y;

        // 상우하좌 순서대로 넣어줍니다.
        int[] dx = new int[]{-1, 0, 1,  0};
        int[] dy = new int[]{0 , 1, 0, -1};

        int moveDir = getSeekerDir();

        // 술래를 한 칸 움직여줍니다.
        seekerPos = new Pair(x + dx[moveDir], y + dy[moveDir]);

        // 끝에 도달했다면 방향을 바꿔줘야 합니다.
        checkFacing();
    }

    public static void getScore(int t) {
        // 상우하좌 순서대로 넣어줍니다.
        int[] dx = new int[]{-1, 0, 1,  0};
        int[] dy = new int[]{0 , 1, 0, -1};

        // 현재 술래의 위치를 불러옵니다.
        int x = seekerPos.x;
        int y = seekerPos.y;

        // 술래의 방향을 불러옵니다.
        int moveDir = getSeekerDir();

        // 3칸을 바라봅니다.
        for(int dist = 0; dist < 3; dist++) {
            int nx = x + dist * dx[moveDir], ny = y + dist * dy[moveDir];

            // 격자를 벗어나지 않으며 나무가 없는 위치라면
            // 도망자들을 전부 잡게 됩니다.
            if(inRange(nx, ny) && !tree[nx][ny]) {
                // 해당 위치의 도망자 수 만큼 점수를 얻게 됩니다.
                ans += t * hiders[nx][ny].size();

                // 도망자들이 사라지게 됩니다.
                hiders[nx][ny] = new ArrayList<>();
            }
        }
    }


    public static void simulate(int t) {
        // 도망자 움직임
        hiderMoveAll();
        // 술래 움직임
        seekerMove();
        // 점수 획득
        getScore(t);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        H = Integer.parseInt(st.nextToken());
        int K = Integer.parseInt(st.nextToken());

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                hiders[i][j] = new ArrayList<>();
            }
        }

        // 도망자 입력
        for (int i = 0; i < M; i++) {
            st = new StringTokenizer(br.readLine());
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
            int d = Integer.parseInt(st.nextToken());
            hiders[x - 1][y - 1].add(d);
        }

        // 나무나무
        for (int i = 0; i < H; i++) {
            st = new StringTokenizer(br.readLine());
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
            tree[x - 1][y - 1] = true;
        }

        seekerPos = new Pair(N / 2, N / 2);

        initSeekerPath();

        for (int t = 1; t <= K; t++) {
            simulate(t);
        }

        System.out.println(ans);
    }

}