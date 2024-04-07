import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;

public class Main {
	final static int BASECAMP = -32; // 베이스캠프(편의점은 최대 30개니 그것보다 많은 32로 설정)
	final static int STORE_VALUE = -1; // 편의점 나타내는 값. STORE*(idx) = 목표 편의점
	final static int DESTROYED = -200; // 사라짐
	
	static int N; // 격자 크기
	static int M; // 사람 수
	
	static Person[] people; // 사람들
	static Pair[] stores; // 편의점들
	static List<Pair> basecamps = new ArrayList<>(); // 베이스캠프들
	static boolean[] isVisited; // 베이스캠프 방문여부
	
	static int[] dx = {-1, 0, 0, 1}; // 상 -> 좌 -> 우 -> 하
	static int[] dy = {0, -1, 1, 0};
	
	static int arriveCnt = 0; // 도착한 사람 명수
	
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		N = Integer.parseInt(st.nextToken());
		M = Integer.parseInt(st.nextToken());
		int[][] arr = new int[N+1][N+1];
		people = new Person[M+1]; // 편리한 인덱싱을 위해 M+1 
		stores = new Pair[M+1];
		
		// 격자 채우기
		for(int i=1; i<=N; i++) {
			st = new StringTokenizer(br.readLine());
			for(int j=1; j<=N; j++) {
				arr[i][j] = Integer.parseInt(st.nextToken());
				if(arr[i][j] == 1) {
					arr[i][j] = BASECAMP;
					basecamps.add(new Pair(i, j));
				}
			}
		}
		isVisited = new boolean[basecamps.size()]; // 방문배열 초기화
		
		int answer = 0;
		// 편의점 받기 
		for(int i=1; i<=M; i++) {
			st = new StringTokenizer(br.readLine());
			int x = Integer.parseInt(st.nextToken());
			int y = Integer.parseInt(st.nextToken());
			arr[x][y] = STORE_VALUE*i;
			stores[i] = new Pair(x, y);
			// 1. 기존 격자에 있는 사람 움직이기
			for(int j=1; j<i; j++) {
				if(people[j].isFinish) continue;
				people[j].setDirection(arr);
				people[j].move();
				if(arr[people[j].pair.x][people[j].pair.y] == STORE_VALUE * j) { // 목표 편의점에 도착했다면,
					people[j].isFinish = true; // 도착
					arr[people[j].pair.x][people[j].pair.y] = DESTROYED; // 파괴
					arriveCnt++;
				}
			}
			// 2. 해당 편의점에서 가장 가까운 베이스캠프 찾아서 i번째 사람이 들어가게 하기
			bfs(arr, x, y, i);
			answer++;
		}
		
		while(arriveCnt != M) { // 모두 도착할 때까지 움직이기
			for(int i=1; i<=M; i++) {
				if(people[i].isFinish) continue;
				people[i].setDirection(arr);
				people[i].move();
			}
			for(int i=1; i<=M; i++) {
				if(arr[people[i].pair.x][people[i].pair.y] == STORE_VALUE * i) { // 목표 편의점에 도착했다면,
					people[i].isFinish = true; // 도착
					arr[people[i].pair.x][people[i].pair.y] = DESTROYED; // 파괴
					arriveCnt++;
				}
			}
			answer++; // 시간 증가
		}
		System.out.println(answer);
	}
	
	private static void bfs(int[][] origin, int x, int y, int idx) { // 현재 편의점에서 가장 가까운 베이스캠프 찾기 찾고나서 해당 베이스캠프는 폐쇄
		int[][] arr = new int[N+1][N+1];
		copy(arr, origin); // 깊은복사
		
		Queue<Pair> queue = new ArrayDeque<>();
		queue.add(new Pair(x, y));
		arr[x][y] = 1;
		
		Pair target = new Pair(-1, -1);
		int minDistance = Integer.MAX_VALUE;
		
		while(!queue.isEmpty()) {
			Pair pair = queue.poll();
			if(arr[pair.x][pair.y] > minDistance) continue; // 이미 최단거리 이상이면 패스
			
			for(int i=0; i<dx.length; i++) {
				int cx = pair.x + dx[i];
				int cy = pair.y + dy[i];
				if(cx <= 0 || cx > N || cy <= 0 || cy > N) continue;  // 범위 쳌
				if(arr[cx][cy] > 0) continue; // 이미 지나간 곳이면 패스
				if(arr[cx][cy] == DESTROYED) continue; // 부서진 곳은 패스
				
				int dist = arr[pair.x][pair.y] + 1;
				if(arr[cx][cy] == BASECAMP) { // 베이스캠프라면 최솟값 업데이트 하고 패스
					if(dist < minDistance) {
						minDistance = dist;
						target.x = cx;
						target.y = cy;
					} else if(dist == minDistance) {
						if(cx < target.x) { // 행이 더 작은 
							target.x = cx;
							target.y = cy;
						} else if(cx == target.x) { // 행이 같다면
							if(cy < target.y) { // 열이 더 작은
								target.x = cx;
								target.y = cy;
							}
						}
					}
					continue;
				}
				queue.add(new Pair(cx,cy));
				arr[cx][cy] = dist; 
			}
		}
		
		origin[target.x][target.y] = DESTROYED; // 해당 위치 폐쇄
		people[idx] = new Person(target, idx); // 베이스캠프에 사람 넣기
	}
	
	private static void print(int[][] arr) { // 디버깅용 함수
		System.out.println();
		for(int i = 1; i<N+1; i++) {
			for(int j=1; j<N+1; j++) {
				System.out.print(arr[i][j] + "\t");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	private static void copy(int[][] newArr, int[][] origin) { // 복사
		for(int i=0; i<origin.length; i++) {
			for(int j=0; j<origin[i].length; j++) {
				newArr[i][j] = origin[i][j];
			}
		}
	}
	
	static class Person {
		Pair pair;
		boolean isFinish = false;
		int dirIdx = -1;
		int idx = 0;

		public Person(Pair pair, int idx) {
			super();
			this.pair = pair;
			this.idx = idx;
		}
		
		void setDirection(int[][] origin) {
			int[][] arr = new int[N+1][N+1];
			copy(arr, origin);
			
			Queue<Pair> queue = new ArrayDeque<>();
			queue.add(new Pair(pair.x, pair.y));
			arr[pair.x][pair.y] = 1;
			
			Pair target = stores[idx];
			int minDist = Integer.MAX_VALUE;
						
			while(!queue.isEmpty()) {
				Pair pair = queue.poll();
				
				if(arr[pair.x][pair.y] > minDist) continue; // 이미 더 큰 경로면 끝
				
				for(int i=0; i<dx.length; i++) {
					int cx = pair.x + dx[i];
					int cy = pair.y + dy[i];
					if(cx <= 0 || cx > N || cy <= 0 || cy > N) continue;  // 범위 쳌
					if(arr[cx][cy] > 0) continue; // 이미 지나간 곳이면 패스
					if(arr[cx][cy] == DESTROYED) continue; // 부서진 곳은 패스
					
					int dist = arr[pair.x][pair.y] + 1;
					if(cx == target.x && cy == target.y) { // 도착했다면
						minDist = dist;
						arr[cx][cy] = dist;
						continue;
					}
					
					queue.add(new Pair(cx,cy));
					arr[cx][cy] = dist; 
				}
			}
			
			// 현재 arr의 타겟에서 출발하여 자기 값 -1 인 방향 찾아서 쭉 가기
			Queue<int[]> q = new ArrayDeque<>();
			for(int i=0; i<dx.length; i++) {
				int cx = target.x + dx[i];
				int cy = target.y + dy[i];
				if(cx <= 0 || cx > N || cy <= 0 || cy > N) continue;
				if(arr[cx][cy] == arr[target.x][target.y] - 1) {
					q.add(new int[] {cx, cy, i}); // x, y, 방향... 이런식으로
				}
			}
			
			int curDir = -1;
			while(!q.isEmpty()) {
				int[] data = q.poll();
				
				if(arr[data[0]][data[1]] == 1) {
					
					curDir = Math.max(curDir, data[data.length-1]);
					continue;
				}
				
				for(int i=0; i<dx.length; i++) {
					int cx = data[0] + dx[i];
					int cy = data[1] + dy[i];
					if(cx <= 0 || cx > N || cy <= 0 || cy > N) continue;
					if(arr[cx][cy] == arr[data[0]][data[1]] - 1) {
						int[] tmpData = new int[data.length+1];
						tmpData[0] = cx;
						tmpData[1] = cy;
						for(int j=2; j<data.length; j++) {
							tmpData[j] = data[j];
						}
						tmpData[data.length] = i; // 현재 방향 넣기
						q.add(tmpData);
					}
				}
			}
			dirIdx = 3 - curDir; // 반대반향으로 돌리기
		}
		
		void move() { // 주어진 방향대로 한칸 가기
			this.pair.x += dx[dirIdx];
			this.pair.y += dy[dirIdx];
		}
	}
	
	static class Pair {
		int x, y;

		public Pair(int x, int y) {
			super();
			this.x = x;
			this.y = y;
		}
	}
}