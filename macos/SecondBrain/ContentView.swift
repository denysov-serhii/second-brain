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

            AIQueryView()
                .tabItem {
                    Label("AI Query", systemImage: "brain")
                }

            ResultFilesView()
                .tabItem {
                    Label("Result Files", systemImage: "doc.text")
                }
        }
        .frame(minWidth: 600, minHeight: 400)
    }
}
