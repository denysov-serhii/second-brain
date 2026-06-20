import SwiftUI
import SwiftData

struct LogsView: View {
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel = LogsViewModel()

    var body: some View {
        VStack(spacing: 0) {
            if viewModel.isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = viewModel.errorMessage {
                VStack(spacing: 12) {
                    Text(error)
                        .foregroundStyle(.secondary)
                    Button("Retry") { viewModel.loadLogs() }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if viewModel.logs.isEmpty {
                Text("No entries yet. Create one using the New Entry tab.")
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                List(viewModel.logs) { log in
                    LogRowView(log: log)
                }
            }
        }
        .toolbar {
            ToolbarItem(placement: .automatic) {
                Button {
                    Task { await viewModel.syncFromServer() }
                } label: {
                    Label("Sync", systemImage: "arrow.triangle.2.circlepath")
                }
            }
        }
        .onAppear {
            viewModel.configure(modelContext: modelContext)
            viewModel.loadLogs()
        }
        .navigationTitle("Logs")
    }
}

private struct LogRowView: View {
    let log: MacLogEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(log.logType ?? "UNKNOWN")
                    .font(.caption)
                    .fontWeight(.semibold)
                    .foregroundStyle(.secondary)
                Spacer()
                Text(log.createdAt, style: .date)
                    .font(.caption2)
                    .foregroundStyle(.tertiary)
            }
            Text(log.summary ?? log.rawContent ?? "")
                .lineLimit(3)
                .font(.body)
            Text(log.sourceDevice)
                .font(.caption2)
                .foregroundStyle(.tertiary)
        }
        .padding(.vertical, 4)
    }
}
