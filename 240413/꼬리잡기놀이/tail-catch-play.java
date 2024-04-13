import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.StringTokenizer;

public class Main {

    static int[] dx = {1, 0, -1, 0};
    static int[] dy = {0, -1, 0, 1};

    static Team[] teams;
    static int[][] teamBoard;
    static int[][] board; // 격자
    static Line[] headers;
    static Line[] tails;
    static int N, M, K; // 격자 크기, 팀 개수, 라운드
    static int curK; // 현재 라운드
    static int score; // 점수

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken()); // 격자크기
        M = Integer.parseInt(st.nextToken()); // 팀 개수
        K = Integer.parseInt(st.nextToken()); // 라운드
        board = new int[N][N];
        teamBoard = new int[N][N];
        teams = new Team[M];
        headers = new Line[M];
        tails = new Line[M];

        for (int i = 0; i < M; i++) {
            teams[i] = new Team();
        }

        // 격자 채우기
        int headersIdx = 0, tailsIdx = 0;
        for (int i = 0; i < N; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < N; j++) {
                board[i][j] = Integer.parseInt(st.nextToken());
                if (board[i][j] == 1) {
                    headers[headersIdx++] = new Line(i, j, 1);
                } else if (board[i][j] == 3) {
                    tails[tailsIdx++] = new Line(i, j, 3);
                }
            }
        }

        // 팀 채우기
        initTeams();

        // K 라운드 진행
        while (K-- > 0) {
            // 1. 머리사람 따라 한 칸 이동
            for (Team team : teams) {
                team.move();
            }
            // 2. 공 던지기
            // 3. 공 맞은 팀의 경우 머리사람, 꼬리사람 바꿈 -> 방향 바꿔짐, 점수 num*num 만큼 획득
            throwBall();

//            print(board);
        }

        System.out.println(score);
    }

    static void print(int[][] arr) {
        System.out.println();
        for (int[] a : arr) {
            System.out.println(Arrays.toString(a));
        }
        System.out.println();
    }

    static void process(int x, int y) {
        int teamIdx = teamBoard[x][y] - 1;
        teams[teamIdx].getPoint(x, y);
        teams[teamIdx].changeOrder();
    }

    private static void throwBall() {
        // 1 <= <= N까지는 curK 행에 대해 던지기 왼->오
        if (curK < N) {
            for (int i = 0; i < N; i++) {
                // 공에 맞으면 머리사람, 꼬리 사람 바꾸기
                if (board[curK][i] >= 1 && board[curK][i] <= 3) {
                    process(curK, i);
                    break;
                }
            }
        }
        // N+1 <= <= 2*N 까지는 아래 -> 위
        else if (curK < 2 * N) {
            for (int i = N - 1; i >= 0; i--) {
                if (board[i][curK - N] >= 1 && board[i][curK - N] <= 3) {
                    process(i, curK - N);
                    break;
                }
            }
        }
        // 2*N+1 <= <= 3*N 까지는 오 ->왼
        else if (curK < 3 * N) {
            for (int i = N - 1; i >= 0; i--) {
                if (board[3 * N - curK - 1][i] >= 1 && board[3 * N - curK - 1][i] <= 3) {
                    process(3 * N - curK - 1, i);
                    break;
                }
            }
        }
        // 3*N+1 <= 4*N 까지는
        else {
            for (int i = 0; i < N; i++) {
                if (board[i][4 * N - curK - 1] >= 1 && board[i][4 * N - curK - 1] <= 3) {
                    process(i, 4 * N - curK - 1);
                    break;
                }
            }
        }
        curK = (curK + 1) % (4 * N);
    }

    // teams 채우기
    private static void initTeams() {
        boolean[][] visited = new boolean[N][N]; // 방문배열

        // 1, 2채우기
        for (int i = 0; i < M; i++) {
            teamBoard[headers[i].x][headers[i].y] = i + 1;
            teams[i].body.add(new Line(headers[i].x, headers[i].y, 1));
            visited[headers[i].x][headers[i].y] = true;
            dfs(i, visited, headers[i].x, headers[i].y, 2); // team 채우기
        }
        // 3, 4 채우기
        for (int i = 0; i < M; i++) {
            teamBoard[tails[i].x][tails[i].y] = i + 1;
            teams[i].body.add(new Line(tails[i].x, tails[i].y, 3));
            visited[tails[i].x][tails[i].y] = true;
            dfs(i, visited, tails[i].x, tails[i].y, 4);
        }
    }

    private static void dfs(int teamIdx, boolean[][] visited, int x, int y, int target) {
        for (int i = 0; i < 4; i++) {
            int cx = x + dx[i];
            int cy = y + dy[i];
            if (cx < 0 || cx >= N || cy < 0 || cy >= N) {
                continue; // 범위 쳌
            }
            if (visited[cx][cy]) {
                continue;
            }
            if (board[cx][cy] == target) {
                if (target == 2) {
                    teams[teamIdx].body.add(new Line(cx, cy, target));
                } else if (target == 4) {
                    teams[teamIdx].path.add(new Line(cx, cy, target));
                }
                teamBoard[cx][cy] = teamIdx + 1;
                visited[cx][cy] = true;
                dfs(teamIdx, visited, cx, cy, target);
            }
        }
    }

    static class Line {

        int x, y;
        int num;

        public Line(int x, int y, int num) {
            this.x = x;
            this.y = y;
            this.num = num;
        }

        @Override
        public String toString() {
            return "Line{" +
                "x=" + x +
                ", y=" + y +
                ", num=" + num +
                '}';
        }
    }

    static class Team {

        ArrayDeque<Line> body = new ArrayDeque<>();
        ArrayDeque<Line> path = new ArrayDeque<>();

        void move() {
//            System.out.println("이동 전");
//            print(board);
            // 1. tail을 path의 첫번째에 넣기(num:4)
            Line tail = body.removeLast();
            tail.num = 4;
            path.addFirst(tail);
            board[tail.x][tail.y] = 4;

            // 2. head의 num을 2로 만들기(num:2)
            body.peek().num = 2;
            board[body.peek().x][body.peek().y] = 2;

            // 3. path의 last를 body의 head로 만들기(num:1)
            Line newHead = path.removeLast();
            newHead.num = 1;
            body.addFirst(newHead);
            board[newHead.x][newHead.y] = 1;

            // 4. body의 last를 tail로 만들기
            body.peekLast().num = 3;
            board[body.peekLast().x][body.peekLast().y] = 3;

//            System.out.println("이동 후");
//            print(board);
        }

        void getPoint(int x, int y) {
            int i = 1;
            for (Line line : body) {
                if (x == line.x && y == line.y) {
                    score += i * i;
                    break;
                }
                i++;
            }
        }

        void changeOrder() {
//            System.out.println(body);
            ArrayDeque<Line> newBody = new ArrayDeque<>();
            while (!body.isEmpty()) {
                Line line = body.removeLast();
                if (line.num == 1) {
                    line.num = 3;
                } else if (line.num == 3) {
                    line.num = 1;
                }
                newBody.offer(line);
            }
            this.body = newBody;
//            System.out.println(newBody);
        }
    }
}