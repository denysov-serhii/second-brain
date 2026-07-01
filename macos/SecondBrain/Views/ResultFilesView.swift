import SwiftUI

struct ResultFilesView: View {
    @State private var viewModel = ResultFilesViewModel()

    var body: some View {
        VStack(spacing: 0) {
            if viewModel.files.isEmpty {
                Text("No AI result files yet. Submit a query in AI Query tab.")
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                List(viewModel.files) { file in
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(file.fileName)
                                .font(.body)
                                .accessibilityLabel("Result file \(file.fileName)")
                            Text(file.createdAt.formatted(date: .abbreviated, time: .shortened))
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }

                        Spacer()

                        Button("Open") {
                            viewModel.open(file)
                        }
                        .buttonStyle(.borderless)

                        Button("Show in Finder") {
                            viewModel.reveal(file)
                        }
                        .buttonStyle(.borderless)
                    }
                }
            }
        }
        .toolbar {
            ToolbarItem(placement: .automatic) {
                Button {
                    viewModel.loadFiles()
                } label: {
                    Label("Refresh", systemImage: "arrow.clockwise")
                }
            }
        }
        .onAppear {
            viewModel.loadFiles()
        }
        .navigationTitle("Result Files")
    }
}
