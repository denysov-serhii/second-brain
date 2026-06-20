import SwiftUI

struct ContentView: View {
    var body: some View {
        TabView {
            LogsView()
                .tabItem {
                    Label("Logs", systemImage: "list.bullet")
                }

            NewLogView()
                .tabItem {
                    Label("New Entry", systemImage: "plus.circle")
                }
        }
        .frame(minWidth: 600, minHeight: 400)
    }
}
